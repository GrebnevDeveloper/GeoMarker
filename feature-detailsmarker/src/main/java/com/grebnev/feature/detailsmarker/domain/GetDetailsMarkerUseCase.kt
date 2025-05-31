package com.grebnev.feature.detailsmarker.domain

import com.grebnev.core.database.repository.GeoMarkerRepository
import javax.inject.Inject

class GetDetailsMarkerUseCase
    @Inject
    constructor(
        private val repository: GeoMarkerRepository,
    ) {
        operator fun invoke(markerId: Long) = repository.getGeoMarkerById(markerId)
    }