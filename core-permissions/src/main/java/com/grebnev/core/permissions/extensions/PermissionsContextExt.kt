package com.grebnev.core.permissions.extensions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

internal fun Context.openAppSettings() {
    startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        },
    )
}