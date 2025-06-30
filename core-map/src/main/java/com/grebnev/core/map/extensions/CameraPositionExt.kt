package com.grebnev.core.map.extensions

import androidx.compose.ui.geometry.Offset
import com.grebnev.core.map.presentation.MapStoreFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow

fun CameraPosition.hasSignificantDifferenceFrom(other: CameraPosition): Boolean =
    abs(this.target.latitude - other.target.latitude) > 0.0001 ||
        abs(this.target.longitude - other.target.longitude) > 0.0001 ||
        abs(this.zoom - other.zoom) > 0.1f

fun CameraPosition.calculateNewPosition(
    panOffset: Offset,
    zoomChange: Float,
): CameraPosition {
    val zoomFactor = ZOOM_POWER_BASE.pow(this.zoom) / ZOOM_FACTOR_DIVIDER

    val latOffset = panOffset.y / (DEFAULT_PIXELS_PER_DEGREE * zoomFactor)
    val lonOffset =
        -panOffset.x / (DEFAULT_PIXELS_PER_DEGREE * zoomFactor * cos(Math.toRadians(this.target.latitude)))

    val adjustedZoomChange = zoomChange * DEFAULT_ZOOM_SENSITIVITY

    return CameraPosition(
        Point(
            this.target.latitude + latOffset,
            this.target.longitude + lonOffset,
        ),
        (this.zoom + adjustedZoomChange).coerceIn(MIN_ZOOM, MAX_ZOOM),
        this.azimuth,
        this.tilt,
    )
}

private const val DEFAULT_PIXELS_PER_DEGREE = 500f
private const val DEFAULT_ZOOM_SENSITIVITY = 5f
private const val ZOOM_FACTOR_DIVIDER = 256f
private const val ZOOM_POWER_BASE = 2f
private const val MIN_ZOOM = MapStoreFactory.MIN_ZOOM
private const val MAX_ZOOM = MapStoreFactory.MAX_ZOOM