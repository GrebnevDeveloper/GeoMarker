package com.grebnev.core.map.domain

import com.grebnev.core.database.repository.position.LastPositionRepository
import com.yandex.mapkit.geometry.Point
import javax.inject.Inject

class UpdateLastPositionUseCase
    @Inject
    constructor(
        private val repository: LastPositionRepository,
    ) {
        suspend operator fun invoke(point: Point) = repository.updateLastPosition(point)
    }