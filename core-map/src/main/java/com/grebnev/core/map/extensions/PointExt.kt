package com.grebnev.core.map.extensions

import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition

val Point.defaultCameraPosition: CameraPosition
    get() = CameraPosition(this, DEFAULT_ZOOM_LEVEL, DEFAULT_AZIMUTH, DEFAULT_TILT)

private const val DEFAULT_ZOOM_LEVEL = 15f
private const val DEFAULT_AZIMUTH = 0f
private const val DEFAULT_TILT = 0f