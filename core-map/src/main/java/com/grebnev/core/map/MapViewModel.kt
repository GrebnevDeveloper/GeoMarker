package com.grebnev.core.map

import androidx.lifecycle.ViewModel
import com.grebnev.core.location.domain.GetCurrentLocationUseCase
import com.grebnev.core.location.domain.ManageLocationUpdatesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MapViewModel
    @Inject
    constructor(
        private val manageLocationUpdates: ManageLocationUpdatesUseCase,
        private val getCurrentLocation: GetCurrentLocationUseCase,
    ) : ViewModel() {
        val locationState = getCurrentLocation()

        fun startUpdates() {
            manageLocationUpdates.startLocationUpdates()
        }

        fun stopUpdates() {
            manageLocationUpdates.stopLocationUpdates()
        }
    }