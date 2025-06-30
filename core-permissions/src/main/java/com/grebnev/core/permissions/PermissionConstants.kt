package com.grebnev.core.permissions

import android.Manifest
import android.os.Build

object PermissionConstants {
    internal const val PREFS_NAME = "permission_prefs"
    internal const val FIRST_LAUNCH_APP = "first_launch_app"
    internal const val FIRST_REQUEST = ".first_request"

    fun requiredPermissions() =
        when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.Q -> {
                setOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                )
            }

            Build.VERSION.SDK_INT in Build.VERSION_CODES.Q..Build.VERSION_CODES.S_V2 -> {
                setOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                )
            }

            else -> {
                setOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_MEDIA_IMAGES,
                )
            }
        }
}