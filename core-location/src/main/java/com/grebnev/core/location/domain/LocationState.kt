package com.grebnev.core.location.domain

import com.yandex.mapkit.geometry.Point

sealed class LocationState {
    data object Initial : LocationState()

    data object Loading : LocationState()

    data class Available(
        val point: Point,
    ) : LocationState()

    data class Error(
        val message: String,
    ) : LocationState()
}