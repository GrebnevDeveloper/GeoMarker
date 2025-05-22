package com.grebnev.core.database.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.grebnev.core.database.database.model.GeoMarkerDbModel

@Dao
interface GeoMarkerDao {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun addToMarker(marker: GeoMarkerDbModel)

    @Query("SELECT * FROM geo_marker")
    suspend fun getMarkers(): List<GeoMarkerDbModel>?

    @Query("SELECT * FROM geo_marker WHERE id=:markerId LIMIT 1")
    suspend fun getMarkerById(markerId: Long): GeoMarkerDbModel?
}