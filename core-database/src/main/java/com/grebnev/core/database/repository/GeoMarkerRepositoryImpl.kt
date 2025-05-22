package com.grebnev.core.database.repository

import com.grebnev.core.database.database.dao.GeoMarkerDao
import com.grebnev.core.database.mapper.toGeoMarker
import com.grebnev.core.database.mapper.toGeoMarkerDbModel
import com.grebnev.core.database.mapper.toGeoMarkers
import com.grebnev.core.domain.entity.GeoMarker
import javax.inject.Inject

class GeoMarkerRepositoryImpl
    @Inject
    constructor(
        private val geoMarkerDao: GeoMarkerDao,
    ) : GeoMarkerRepository {
        override suspend fun addGeoMarker(marker: GeoMarker) =
            geoMarkerDao.addToMarker(marker.toGeoMarkerDbModel())

        override suspend fun getGeoMarkers(): List<GeoMarker> =
            geoMarkerDao.getMarkers()?.toGeoMarkers() ?: emptyList()

        override suspend fun getGeoMarkerById(markerId: Long): GeoMarker =
            geoMarkerDao.getMarkerById(markerId)?.toGeoMarker()
                ?: throw NoSuchElementException("Marker with id $markerId not found")
    }