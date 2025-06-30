package com.grebnev.core.map.domain

import com.grebnev.core.database.repository.position.LastPositionRepository
import javax.inject.Inject

class GetLastPositionUseCase
    @Inject
    constructor(
        private val repository: LastPositionRepository,
    ) {
        suspend operator fun invoke() = repository.getLastPosition()
    }