package com.grebnev.feature.addmarker.domain

import com.grebnev.core.database.repository.marker.GeoMarkerRepository
import com.grebnev.core.domain.entity.GeoMarker
import javax.inject.Inject

class AddGeoMarkerUseCase
    @Inject
    constructor(
        private val geoMarkerRepository: GeoMarkerRepository,
    ) {
        suspend operator fun invoke(geoMarker: GeoMarker) = geoMarkerRepository.addGeoMarker(geoMarker)
    }