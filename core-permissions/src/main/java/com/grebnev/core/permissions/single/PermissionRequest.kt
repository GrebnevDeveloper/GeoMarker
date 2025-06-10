package com.grebnev.core.permissions.single

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.grebnev.core.permissions.PermissionConstants
import com.grebnev.core.permissions.extensions.openAppSettings
import com.grebnev.core.permissions.extensions.toReadableName

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionRequest(
    permission: String,
    content: @Composable (PermissionState, () -> Unit) -> Unit,
) {
    val context = LocalContext.current
    val permissionState = rememberPermissionState(permission)
    var showDenied by remember { mutableStateOf(false) }

    val isFirstRequest =
        remember {
            val prefs = context.getSharedPreferences(PermissionConstants.PREFS_NAME, Context.MODE_PRIVATE)
            mutableStateOf(prefs.getBoolean("$permission${PermissionConstants.FIRST_REQUEST}", true))
        }

    val onPermissionCheck = {
        when {
            permissionState.status.isGranted -> {
            }

            else -> {
                val isPermanentlyDenied =
                    when (permissionState.status) {
                        is PermissionStatus.Denied ->
                            !(permissionState.status as PermissionStatus.Denied).shouldShowRationale

                        else -> false
                    }

                if (isPermanentlyDenied && !isFirstRequest.value) {
                    showDenied = true
                } else {
                    if (isFirstRequest.value) {
                        context
                            .getSharedPreferences(PermissionConstants.PREFS_NAME, Context.MODE_PRIVATE)
                            .edit { putBoolean("$permission${PermissionConstants.FIRST_REQUEST}", false) }
                        isFirstRequest.value = false
                    }
                    permissionState.launchPermissionRequest()
                }
            }
        }
    }

    if (showDenied) {
        PermissionDeniedDialog(
            permissionText = permissionState.permission.toReadableName(context),
            onOpenSettings = {
                context.openAppSettings()
                showDenied = false
            },
            onDismiss = { showDenied = false },
        )
    }

    content(permissionState, onPermissionCheck)
}