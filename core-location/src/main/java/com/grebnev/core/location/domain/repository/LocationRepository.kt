package com.grebnev.core.location.domain.repository

import com.grebnev.core.location.domain.entity.LocationStatus
import kotlinx.coroutines.flow.StateFlow

interface LocationRepository {
    val getCurrentLocation: StateFlow<LocationStatus>

    fun startLocationUpdates(minInterval: Long = 10000L)

    fun stopLocationUpdates()
}