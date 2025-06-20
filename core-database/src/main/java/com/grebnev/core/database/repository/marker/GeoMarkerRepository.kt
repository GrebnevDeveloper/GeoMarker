package com.grebnev.core.database.repository.marker

import com.grebnev.core.common.Result
import com.grebnev.core.domain.entity.GeoMarker
import kotlinx.coroutines.flow.Flow

interface GeoMarkerRepository {
    suspend fun addGeoMarker(marker: GeoMarker)

    suspend fun deleteMarkerById(markerId: Long)

    fun getGeoMarkers(): Flow<List<GeoMarker>>

    fun getGeoMarkerById(markerId: Long): Flow<Result<GeoMarker>>
}