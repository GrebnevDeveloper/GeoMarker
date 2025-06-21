package com.grebnev.feature.editormarker.domain

import com.grebnev.core.database.repository.marker.GeoMarkerRepository
import javax.inject.Inject

class DeleteMarkerUseCase
    @Inject
    constructor(
        private val repository: GeoMarkerRepository,
    ) {
        suspend operator fun invoke(markerId: Long) = repository.deleteMarkerById(markerId)
    }