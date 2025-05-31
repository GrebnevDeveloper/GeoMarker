package com.grebnev.core.database.repository

import com.grebnev.core.database.database.dao.GeoMarkerDao
import com.grebnev.core.database.mapper.toGeoMarker
import com.grebnev.core.database.mapper.toGeoMarkerDbModel
import com.grebnev.core.database.mapper.toGeoMarkers
import com.grebnev.core.domain.entity.GeoMarker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GeoMarkerRepositoryImpl
    @Inject
    constructor(
        private val geoMarkerDao: GeoMarkerDao,
    ) : GeoMarkerRepository {
        override suspend fun addGeoMarker(marker: GeoMarker) =
            geoMarkerDao.addToMarker(marker.toGeoMarkerDbModel())

        override fun getGeoMarkers(): Flow<List<GeoMarker>> =
            geoMarkerDao.getMarkers().map { it.toGeoMarkers() }

        override fun getGeoMarkerById(markerId: Long): Flow<GeoMarker> =
            geoMarkerDao.getMarkerById(markerId).map { it.toGeoMarker() }
    }