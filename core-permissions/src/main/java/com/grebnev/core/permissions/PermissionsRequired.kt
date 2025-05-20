package com.grebnev.core.permissions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionRequired(
    context: Context,
    permission: String,
    permissionDescription: String,
    content: @Composable (PermissionState) -> Unit,
) {
    val permissionState = rememberPermissionState(permission)
    var showRationale by remember { mutableStateOf(false) }
    var showDenied by remember { mutableStateOf(false) }

    LaunchedEffect(permissionState.status) {
        when (permissionState.status) {
            is PermissionStatus.Granted -> {
                showRationale = false
                showDenied = false
            }
            is PermissionStatus.Denied -> {
                val shouldShowRationale =
                    (permissionState.status as PermissionStatus.Denied)
                        .shouldShowRationale

                if (shouldShowRationale) {
                    showRationale = true
                } else {
                    showDenied = true
                }
            }
        }
    }

    if (showRationale) {
        PermissionRationaleDialog(
            permissionText = permissionDescription,
            onConfirm = {
                permissionState.launchPermissionRequest()
                showRationale = false
            },
            onDismiss = { showRationale = false },
        )
    }

    if (showDenied) {
        PermissionDeniedDialog(
            permissionText = permissionDescription,
            onOpenSettings = {
                context.openAppSettings()
                showDenied = false
            },
            onDismiss = { showDenied = false },
        )
    }

    content(permissionState)
}

fun Context.openAppSettings() {
    startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        },
    )
}