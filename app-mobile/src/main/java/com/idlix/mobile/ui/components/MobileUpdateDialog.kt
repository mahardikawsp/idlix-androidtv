package com.idlix.mobile.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.idlix.data.update.AppUpdateErrorType
import com.idlix.data.update.AppUpdateState
import com.idlix.mobile.R
import com.idlix.mobile.ui.theme.IdlixRed
import com.idlix.mobile.ui.theme.SurfaceDark
import com.idlix.mobile.ui.theme.TextMuted
import com.idlix.mobile.ui.theme.TextWhite

@Composable
fun MobileUpdateDialog(
    latestVersion: String,
    currentVersion: String,
    onInstallNow: () -> Unit,
    onDismiss: () -> Unit
) {
    val appName = stringResource(R.string.app_name)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        shape = RoundedCornerShape(16.dp),
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(IdlixRed.copy(alpha = 0.16f), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdateAlt,
                            contentDescription = null,
                            tint = IdlixRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(IdlixRed, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.update_dialog_badge),
                            color = TextWhite,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.update_dialog_title, latestVersion),
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Text(
                text = stringResource(
                    R.string.update_dialog_message,
                    appName,
                    latestVersion,
                    currentVersion
                ),
                color = TextMuted,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onInstallNow,
                colors = ButtonDefaults.buttonColors(
                    containerColor = IdlixRed,
                    contentColor = TextWhite
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SystemUpdateAlt,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.update_dialog_install_now),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.update_dialog_later),
                    color = TextMuted
                )
            }
        }
    )
}

@Composable
fun resolveMobileUpdateErrorMessage(error: AppUpdateState.Error): String {
    return when (error.errorType) {
        AppUpdateErrorType.NETWORK_ERROR -> stringResource(R.string.update_error_network)
        AppUpdateErrorType.HTTP_ERROR -> stringResource(R.string.update_error_http, error.httpCode ?: 0)
        AppUpdateErrorType.INVALID_RESPONSE -> stringResource(R.string.update_error_invalid_response)
        AppUpdateErrorType.DOWNLOAD_FAILED -> stringResource(R.string.update_error_download_failed)
        AppUpdateErrorType.INVALID_APK -> stringResource(R.string.update_error_invalid_apk)
        AppUpdateErrorType.INSTALL_FAILED -> stringResource(R.string.update_error_install_failed)
    }
}

