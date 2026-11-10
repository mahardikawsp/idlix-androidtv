package com.idlix.data.update

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.idlix.data.api.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

enum class AppUpdateErrorType {
    NETWORK_ERROR,
    HTTP_ERROR,
    INVALID_RESPONSE,
    DOWNLOAD_FAILED,
    INVALID_APK,
    INSTALL_FAILED
}

sealed interface AppUpdateState {
    data object Idle : AppUpdateState
    data object Checking : AppUpdateState
    data class UpToDate(val currentVersion: String) : AppUpdateState
    data class Downloading(
        val latestVersion: String,
        val progressPercent: Int,
        val downloadedBytes: Long,
        val totalBytes: Long
    ) : AppUpdateState
    data class ReadyToInstall(
        val latestVersion: String,
        val apkFile: File
    ) : AppUpdateState
    data class Error(
        val errorType: AppUpdateErrorType,
        val httpCode: Int? = null,
        val detail: String? = null
    ) : AppUpdateState
}

object AppUpdateManager {

    private const val RELEASES_INI_BASE_URL = "https://cdn.abdilah.id/shared/idlix/releases.ini"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _state = MutableStateFlow<AppUpdateState>(AppUpdateState.Idle)
    val state: StateFlow<AppUpdateState> = _state.asStateFlow()

    private val _showUpdatePrompt = MutableStateFlow(false)
    val showUpdatePrompt: StateFlow<Boolean> = _showUpdatePrompt.asStateFlow()

    @Volatile
    private var hasCheckedOnStartup = false

    @Volatile
    private var activeJob: Job? = null

    @Volatile
    private var pendingInstallFile: File? = null

    fun checkOnStartup(
        context: Context,
        platform: AppPlatform,
        currentVersion: String
    ) {
        if (hasCheckedOnStartup) return
        hasCheckedOnStartup = true
        checkForUpdate(
            context = context.applicationContext,
            platform = platform,
            currentVersion = currentVersion,
            isManualCheck = false
        )
    }

    fun checkForUpdate(
        context: Context,
        platform: AppPlatform,
        currentVersion: String,
        isManualCheck: Boolean = true
    ) {
        val currentState = _state.value
        if (currentState is AppUpdateState.Checking || currentState is AppUpdateState.Downloading) {
            return
        }

        val appContext = context.applicationContext
        activeJob?.cancel()
        activeJob = scope.launch {
            _state.value = AppUpdateState.Checking

            val timestamp = System.currentTimeMillis()
            val requestUrl = "$RELEASES_INI_BASE_URL?ts=$timestamp"

            val iniContent = try {
                val request = Request.Builder()
                    .url(requestUrl)
                    .get()
                    .build()

                ApiClient.updateHttpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        _state.value = AppUpdateState.Error(
                            errorType = AppUpdateErrorType.HTTP_ERROR,
                            httpCode = response.code
                        )
                        return@launch
                    }
                    response.body?.string().orEmpty()
                }
            } catch (e: Exception) {
                _state.value = AppUpdateState.Error(
                    errorType = AppUpdateErrorType.NETWORK_ERROR,
                    detail = e.localizedMessage
                )
                return@launch
            }

            val sectionInfo = ReleaseIniParser.parse(iniContent, platform)
            if (sectionInfo == null) {
                _state.value = AppUpdateState.Error(
                    errorType = AppUpdateErrorType.INVALID_RESPONSE
                )
                return@launch
            }

            val remoteVersion = ReleaseIniParser.normalizeVersion(sectionInfo.version)
            val cleanCurrentVersion = ReleaseIniParser.normalizeVersion(currentVersion)

            if (!ReleaseIniParser.isUpdateRequired(cleanCurrentVersion, remoteVersion)) {
                cleanupOldApks(appContext, keepFileName = null)
                _state.value = AppUpdateState.UpToDate(cleanCurrentVersion)
                return@launch
            }

            // Update diperlukan: periksa apakah APK versi terbaru sudah terunduh di cache
            val updatesDir = File(appContext.cacheDir, "updates").apply { mkdirs() }
            val safeVersionToken = remoteVersion.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            val targetApkFile = File(updatesDir, "idlix_${platform.name.lowercase()}_$safeVersionToken.apk")

            if (targetApkFile.exists() && isValidApk(appContext, targetApkFile)) {
                cleanupOldApks(appContext, keepFileName = targetApkFile.name)
                _state.value = AppUpdateState.ReadyToInstall(
                    latestVersion = remoteVersion,
                    apkFile = targetApkFile
                )
                _showUpdatePrompt.value = true
                return@launch
            }

            // Hapus APK lama sebelum mengunduh yang baru
            cleanupOldApks(appContext, keepFileName = null)

            downloadApk(
                context = appContext,
                downloadUrl = sectionInfo.link,
                latestVersion = remoteVersion,
                targetFile = targetApkFile
            )
        }
    }

    private fun downloadApk(
        context: Context,
        downloadUrl: String,
        latestVersion: String,
        targetFile: File
    ) {
        val tempFile = File(targetFile.parentFile, "${targetFile.name}.tmp")
        try {
            if (tempFile.exists()) {
                tempFile.delete()
            }

            val request = Request.Builder()
                .url(downloadUrl)
                .get()
                .build()

            ApiClient.updateHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    _state.value = AppUpdateState.Error(
                        errorType = AppUpdateErrorType.HTTP_ERROR,
                        httpCode = response.code
                    )
                    return
                }

                val body = response.body
                if (body == null) {
                    _state.value = AppUpdateState.Error(
                        errorType = AppUpdateErrorType.DOWNLOAD_FAILED
                    )
                    return
                }

                val totalBytes = body.contentLength()
                var downloadedBytes = 0L
                var lastReportedPercent = -1
                var lastReportTimeMs = 0L

                _state.value = AppUpdateState.Downloading(
                    latestVersion = latestVersion,
                    progressPercent = 0,
                    downloadedBytes = 0L,
                    totalBytes = totalBytes.coerceAtLeast(0L)
                )

                body.byteStream().use { input ->
                    FileOutputStream(tempFile).use { output ->
                        val buffer = ByteArray(8 * 1024)
                        var bytesRead: Int
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            downloadedBytes += bytesRead

                            val percent = if (totalBytes > 0L) {
                                ((downloadedBytes * 100L) / totalBytes).toInt().coerceIn(0, 100)
                            } else {
                                0
                            }

                            val now = System.currentTimeMillis()
                            if (percent != lastReportedPercent || now - lastReportTimeMs >= 200L) {
                                lastReportedPercent = percent
                                lastReportTimeMs = now
                                _state.value = AppUpdateState.Downloading(
                                    latestVersion = latestVersion,
                                    progressPercent = percent,
                                    downloadedBytes = downloadedBytes,
                                    totalBytes = totalBytes.coerceAtLeast(0L)
                                )
                            }
                        }
                        output.flush()
                    }
                }
            }

            if (!tempFile.exists() || tempFile.length() == 0L || !isValidApk(context, tempFile)) {
                tempFile.delete()
                _state.value = AppUpdateState.Error(
                    errorType = AppUpdateErrorType.INVALID_APK
                )
                return
            }

            if (targetFile.exists()) {
                targetFile.delete()
            }
            val renamed = tempFile.renameTo(targetFile)
            val finalFile = if (renamed) targetFile else tempFile

            _state.value = AppUpdateState.ReadyToInstall(
                latestVersion = latestVersion,
                apkFile = finalFile
            )
            _showUpdatePrompt.value = true
        } catch (e: Exception) {
            tempFile.delete()
            _state.value = AppUpdateState.Error(
                errorType = AppUpdateErrorType.DOWNLOAD_FAILED,
                detail = e.localizedMessage
            )
        }
    }

    private fun isValidApk(context: Context, file: File): Boolean {
        if (!file.exists() || file.length() <= 0L) return false
        return try {
            val archiveInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageArchiveInfo(
                    file.absolutePath,
                    PackageManager.PackageInfoFlags.of(0L)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageArchiveInfo(file.absolutePath, 0)
            }
            archiveInfo != null
        } catch (_: Exception) {
            false
        }
    }

    private fun cleanupOldApks(context: Context, keepFileName: String?) {
        try {
            val updatesDir = File(context.cacheDir, "updates")
            if (!updatesDir.exists()) return
            updatesDir.listFiles()?.forEach { file ->
                if (keepFileName == null || file.name != keepFileName) {
                    file.delete()
                }
            }
        } catch (_: Exception) {
        }
    }

    fun dismissUpdatePrompt() {
        _showUpdatePrompt.value = false
    }

    fun requestShowUpdatePrompt() {
        if (_state.value is AppUpdateState.ReadyToInstall) {
            _showUpdatePrompt.value = true
        }
    }

    fun installApk(context: Context, apkFile: File): Boolean {
        if (!apkFile.exists()) {
            _state.value = AppUpdateState.Error(errorType = AppUpdateErrorType.INVALID_APK)
            return false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canInstall = try {
                context.packageManager.canRequestPackageInstalls()
            } catch (_: Exception) {
                true
            }

            if (!canInstall) {
                pendingInstallFile = apkFile
                try {
                    val permissionIntent = Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:${context.packageName}")
                    ).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(permissionIntent)
                    return true
                } catch (_: ActivityNotFoundException) {
                    // Beberapa ROM Android TV tidak memiliki layar per-package unknown sources;
                    // lanjutkan langsung ke ACTION_VIEW installer paket.
                    pendingInstallFile = null
                } catch (_: Exception) {
                    pendingInstallFile = null
                }
            }
        }

        return launchPackageInstaller(context, apkFile)
    }

    fun resumePendingInstallIfAllowed(context: Context) {
        val pendingFile = pendingInstallFile ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canInstall = try {
                context.packageManager.canRequestPackageInstalls()
            } catch (_: Exception) {
                false
            }
            if (canInstall && pendingFile.exists()) {
                pendingInstallFile = null
                launchPackageInstaller(context, pendingFile)
            }
        }
    }

    private fun launchPackageInstaller(context: Context, apkFile: File): Boolean {
        return try {
            val authority = "${context.packageName}.fileprovider"
            val apkUri = FileProvider.getUriForFile(context, authority, apkFile)

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(installIntent)
            _showUpdatePrompt.value = false
            true
        } catch (e: Exception) {
            _state.value = AppUpdateState.Error(
                errorType = AppUpdateErrorType.INSTALL_FAILED,
                detail = e.localizedMessage
            )
            false
        }
    }
}

