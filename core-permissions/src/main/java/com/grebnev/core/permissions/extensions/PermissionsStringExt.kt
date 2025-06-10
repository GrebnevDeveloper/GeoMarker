package com.grebnev.core.permissions.extensions

import android.Manifest
import android.content.Context
import com.grebnev.core.permissions.R

internal fun String.toReadableName(context: Context): String =
    when (this) {
        Manifest.permission.CAMERA -> context.getString(R.string.camera_access)
        Manifest.permission.ACCESS_FINE_LOCATION -> context.getString(R.string.location_access)
        Manifest.permission.ACCESS_COARSE_LOCATION -> context.getString(R.string.location_access)
        Manifest.permission.READ_MEDIA_IMAGES -> context.getString(R.string.media_access)
        Manifest.permission.READ_EXTERNAL_STORAGE -> context.getString(R.string.read_storage_access)
        Manifest.permission.WRITE_EXTERNAL_STORAGE -> context.getString(R.string.write_storage_access)
        else ->
            this
                .substringAfterLast(".")
                .replace("_", " ")
                .lowercase()
                .replaceFirstChar { it.uppercase() }
    }