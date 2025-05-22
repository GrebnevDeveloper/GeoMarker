package com.grebnev.core.database.repository

import com.grebnev.core.domain.entity.GeoMarker

interface GeoMarkerRepository {
    suspend fun addGeoMarker(marker: GeoMarker)

    suspend fun getGeoMarkers(): List<GeoMarker>

    suspend fun getGeoMarkerById(markerId: Long): GeoMarker
}