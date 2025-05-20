package com.grebnev.core.location.domain

import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GetCurrentLocationUseCase
    @Inject
    constructor(
        private val repository: LocationRepository,
    ) {
        operator fun invoke(): StateFlow<LocationState> = repository.getCurrentLocation
    }