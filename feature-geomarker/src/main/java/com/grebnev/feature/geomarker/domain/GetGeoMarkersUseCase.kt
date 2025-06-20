package com.grebnev.feature.geomarker.domain

import com.grebnev.core.database.repository.marker.GeoMarkerRepository
import javax.inject.Inject

class GetGeoMarkersUseCase
    @Inject
    constructor(
        private val repository: GeoMarkerRepository,
    ) {
        operator fun invoke() = repository.getGeoMarkers()
    }