package com.grebnev.core.database.repository.position

import com.grebnev.core.database.database.dao.MetadataDao
import com.yandex.mapkit.geometry.Point
import javax.inject.Inject

class LastPositionRepositoryImpl
    @Inject
    constructor(
        private val metadataDao: MetadataDao,
    ) : LastPositionRepository {
        override suspend fun getLastPosition(): Point? = metadataDao.getLastPosition()

        override suspend fun updateLastPosition(point: Point) {
            metadataDao.updateLastPosition(point)
        }
    }