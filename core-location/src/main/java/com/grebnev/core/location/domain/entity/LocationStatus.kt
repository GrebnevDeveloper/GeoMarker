package com.grebnev.core.location.domain.entity

import com.yandex.mapkit.geometry.Point

sealed class LocationStatus {
    data object Initial : LocationStatus()

    data object Loading : LocationStatus()

    data class Available(
        val point: Point,
    ) : LocationStatus()

    data class Error(
        val message: String,
    ) : LocationStatus()
}