package com.grebnev.core.database.repository

import com.grebnev.core.domain.entity.GeoMarker
import kotlinx.coroutines.flow.Flow

interface GeoMarkerRepository {
    suspend fun addGeoMarker(marker: GeoMarker)

    fun getGeoMarkers(): Flow<List<GeoMarker>>

    suspend fun getGeoMarkerById(markerId: Long): GeoMarker
}