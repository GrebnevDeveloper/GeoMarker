package com.grebnev.core.location.domain.usecase

import com.grebnev.core.location.domain.repository.LocationRepository
import javax.inject.Inject

class ManageLocationUpdatesUseCase
    @Inject
    constructor(
        private val repository: LocationRepository,
    ) {
        fun startLocationUpdates() = repository.startLocationUpdates()

        fun stopLocationUpdates() = repository.stopLocationUpdates()
    }