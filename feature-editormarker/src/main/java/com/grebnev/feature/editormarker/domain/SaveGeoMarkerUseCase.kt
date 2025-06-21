package com.grebnev.feature.editormarker.domain

import com.grebnev.core.database.repository.marker.GeoMarkerRepository
import com.grebnev.core.domain.entity.GeoMarker
import javax.inject.Inject

class SaveGeoMarkerUseCase
    @Inject
    constructor(
        private val geoMarkerRepository: GeoMarkerRepository,
    ) {
        suspend operator fun invoke(geoMarker: GeoMarker) = geoMarkerRepository.saveGeoMarker(geoMarker)
    }