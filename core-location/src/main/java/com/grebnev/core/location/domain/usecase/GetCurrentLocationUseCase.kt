package com.grebnev.core.location.domain.usecase

import com.grebnev.core.location.domain.entity.LocationStatus
import com.grebnev.core.location.domain.repository.LocationRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GetCurrentLocationUseCase
    @Inject
    constructor(
        private val repository: LocationRepository,
    ) {
        operator fun invoke(): StateFlow<LocationStatus> = repository.getCurrentLocation
    }