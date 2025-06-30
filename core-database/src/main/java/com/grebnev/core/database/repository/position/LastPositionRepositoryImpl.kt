package com.grebnev.core.database.repository.position

import com.grebnev.core.database.database.converter.PositionConverter
import com.grebnev.core.database.database.dao.MetadataDao
import com.grebnev.core.database.database.model.MetadataDbModel
import com.yandex.mapkit.geometry.Point
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LastPositionRepositoryImpl
    @Inject
    constructor(
        private val metadataDao: MetadataDao,
    ) : LastPositionRepository {
        override suspend fun getLastPosition(): Point? {
            val metadata = metadataDao.getMetadataByKey(LAST_POSITION_KEY)
            return metadata?.let { PositionConverter.fromString(metadata) }
        }

        override fun getLastPositionFlow(): Flow<Point?> =
            metadataDao.getMetadataFlowByKey(LAST_POSITION_KEY).map { metadata ->
                metadata?.let {
                    PositionConverter.fromString(metadata)
                }
            }

        override suspend fun updateLastPosition(point: Point) {
            val metadata =
                MetadataDbModel(
                    keyMetadata = LAST_POSITION_KEY,
                    value = PositionConverter.toString(point),
                )

            metadataDao.updateMetadata(metadata)
        }

        companion object {
            private const val LAST_POSITION_KEY = "last_position"
        }
    }