package com.grebnev.core.location.domain

import kotlinx.coroutines.flow.StateFlow

interface LocationRepository {
    val getCurrentLocation: StateFlow<LocationState>

    fun startLocationUpdates(minInterval: Long = 10000L)

    fun stopLocationUpdates()
}