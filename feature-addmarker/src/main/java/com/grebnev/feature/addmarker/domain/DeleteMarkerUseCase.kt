package com.grebnev.feature.addmarker.domain

import com.grebnev.core.database.repository.GeoMarkerRepository
import javax.inject.Inject

class DeleteMarkerUseCase
    @Inject
    constructor(
        private val repository: GeoMarkerRepository,
    ) {
        suspend operator fun invoke(markerId: Long) = repository.deleteMarkerById(markerId)
    }