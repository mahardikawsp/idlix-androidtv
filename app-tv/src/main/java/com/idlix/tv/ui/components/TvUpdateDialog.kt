package com.idlix.tv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdateAlt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.idlix.data.update.AppUpdateErrorType
import com.idlix.data.update.AppUpdateState
import com.idlix.tv.R
import com.idlix.tv.ui.theme.NetflixBlack
import com.idlix.tv.ui.theme.NetflixDarkCard
import com.idlix.tv.ui.theme.NetflixLightGray
import com.idlix.tv.ui.theme.NetflixRed
import com.idlix.tv.ui.theme.NetflixWhite

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvUpdateDialog(
    latestVersion: String,
    currentVersion: String,
    onInstallNow: () -> Unit,
    onDismiss: () -> Unit
) {
    val installFocusRequester = remember { FocusRequester() }
    val appName = stringResource(R.string.app_name)

    LaunchedEffect(Unit) {
        try {
            installFocusRequester.requestFocus()
        } catch (_: Exception) {
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .width(460.dp)
                .background(NetflixDarkCard, RoundedCornerShape(12.dp))
                .border(1.dp, NetflixWhite.copy(alpha = 0.14f), RoundedCornerShape(12.dp))
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(NetflixRed.copy(alpha = 0.18f), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdateAlt,
                            contentDescription = null,
                            tint = NetflixRed,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(NetflixRed, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.update_dialog_badge),
                            color = NetflixWhite,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.update_dialog_title, latestVersion),
                    color = NetflixWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = stringResource(
                        R.string.update_dialog_message,
                        appName,
                        latestVersion,
                        currentVersion
                    ),
                    color = NetflixLightGray,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.colors(
                            containerColor = NetflixBlack,
                            focusedContainerColor = NetflixWhite,
                            contentColor = NetflixLightGray,
                            focusedContentColor = NetflixBlack
                        ),
                        shape = ButtonDefaults.shape(RoundedCornerShape(6.dp)),
                        modifier = Modifier.height(42.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.update_dialog_later),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }

                    Button(
                        onClick = onInstallNow,
                        colors = ButtonDefaults.colors(
                            containerColor = NetflixRed,
                            focusedContainerColor = NetflixWhite,
                            contentColor = NetflixWhite,
                            focusedContentColor = NetflixBlack
                        ),
                        shape = ButtonDefaults.shape(RoundedCornerShape(6.dp)),
                        modifier = Modifier
                            .height(42.dp)
                            .focusRequester(installFocusRequester)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdateAlt,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.update_dialog_install_now),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun resolveTvUpdateErrorMessage(error: AppUpdateState.Error): String {
    return when (error.errorType) {
        AppUpdateErrorType.NETWORK_ERROR -> stringResource(R.string.update_error_network)
        AppUpdateErrorType.HTTP_ERROR -> stringResource(R.string.update_error_http, error.httpCode ?: 0)
        AppUpdateErrorType.INVALID_RESPONSE -> stringResource(R.string.update_error_invalid_response)
        AppUpdateErrorType.DOWNLOAD_FAILED -> stringResource(R.string.update_error_download_failed)
        AppUpdateErrorType.INVALID_APK -> stringResource(R.string.update_error_invalid_apk)
        AppUpdateErrorType.INSTALL_FAILED -> stringResource(R.string.update_error_install_failed)
    }
}

