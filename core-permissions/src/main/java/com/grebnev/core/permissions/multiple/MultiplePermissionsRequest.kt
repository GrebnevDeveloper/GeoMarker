package com.grebnev.core.permissions.multiple

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale
import com.grebnev.core.permissions.PermissionConstants
import com.grebnev.core.permissions.extensions.openAppSettings

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MultiplePermissionsRequest(
    permissions: Set<String>,
    content: @Composable (List<PermissionState>) -> Unit,
) {
    val context = LocalContext.current
    val permissionsState = rememberMultiplePermissionsState(permissions.toList())
    var showDeniedDialog by remember { mutableStateOf(false) }

    val isFirstLaunch =
        remember {
            val prefs = context.getSharedPreferences(PermissionConstants.PREFS_NAME, Context.MODE_PRIVATE)
            val firstLaunch = prefs.getBoolean(PermissionConstants.FIRST_LAUNCH_APP, true)
            if (firstLaunch) {
                prefs.edit { putBoolean(PermissionConstants.FIRST_LAUNCH_APP, false) }
            }
            firstLaunch
        }

    LaunchedEffect(Unit) {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        }
    }

    LaunchedEffect(permissionsState.permissions) {
        if (!isFirstLaunch) {
            var hasPermanentDenial = false

            permissionsState.permissions.forEach { permission ->
                if (!permission.status.isGranted) {
                    val isPermanent = !permission.status.shouldShowRationale
                    if (isPermanent) hasPermanentDenial = true
                }
            }
            showDeniedDialog = hasPermanentDenial
        }
    }

    if (showDeniedDialog) {
        MultiplePermissionsDeniedDialog(
            context = context,
            permissionsText =
                permissionsState.permissions
                    .filter { !it.status.isGranted }
                    .map { it.permission }
                    .toSet(),
            onOpenSettings = {
                context.openAppSettings()
                showDeniedDialog = false
            },
            onDismiss = { showDeniedDialog = false },
        )
    }

    content(permissionsState.permissions)
}