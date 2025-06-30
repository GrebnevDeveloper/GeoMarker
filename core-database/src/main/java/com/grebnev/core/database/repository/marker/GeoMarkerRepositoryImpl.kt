package com.grebnev.core.database.repository.marker

import com.grebnev.core.common.wrappers.Result
import com.grebnev.core.database.database.dao.GeoMarkerDao
import com.grebnev.core.database.mapper.toGeoMarker
import com.grebnev.core.database.mapper.toGeoMarkerDbModel
import com.grebnev.core.database.mapper.toGeoMarkers
import com.grebnev.core.domain.entity.GeoMarker
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import javax.inject.Inject

class GeoMarkerRepositoryImpl
    @Inject
    constructor(
        private val geoMarkerDao: GeoMarkerDao,
    ) : GeoMarkerRepository {
        override suspend fun saveGeoMarker(marker: GeoMarker) =
            geoMarkerDao.saveGeoMarker(marker.toGeoMarkerDbModel())

        override suspend fun deleteMarkerById(markerId: Long) {
            geoMarkerDao.deleteMarkerById(markerId)
        }

        override fun getGeoMarkers(): Flow<Result<List<GeoMarker>>> =
            geoMarkerDao
                .getMarkers()
                .map { dbModel ->
                    dbModel.toGeoMarkers().let { markers ->
                        when {
                            markers.isNotEmpty() -> Result.success(markers)
                            else -> Result.empty()
                        }
                    }
                }.retryWhen { cause, attempt ->
                    if (attempt <= MAX_COUNT_RETRY) {
                        delay(RETRY_TIMEOUT)
                    } else {
                        emit(Result.error(cause))
                        delay(RETRY_TIMEOUT * 2)
                    }
                    true
                }

        override fun getGeoMarkerById(markerId: Long): Flow<Result<GeoMarker>> =
            geoMarkerDao
                .getMarkerById(markerId)
                .map { dbModel ->
                    dbModel?.toGeoMarker()?.let { Result.success(it) } ?: Result.empty()
                }.catch { exception ->
                    emit(Result.error(exception))
                }

        companion object {
            private const val MAX_COUNT_RETRY = 3L
            private const val RETRY_TIMEOUT = 3000L
        }
    }