package com.grebnev.core.database.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.grebnev.core.database.database.model.GeoMarkerDbModel
import kotlinx.coroutines.flow.Flow

@Dao
interface GeoMarkerDao {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun saveGeoMarker(marker: GeoMarkerDbModel)

    @Query("DELETE FROM geo_marker WHERE id=:markerId")
    suspend fun deleteMarkerById(markerId: Long)

    @Query("SELECT * FROM geo_marker")
    fun getMarkers(): Flow<List<GeoMarkerDbModel>>

    @Query("SELECT * FROM geo_marker WHERE id=:markerId LIMIT 1")
    fun getMarkerById(markerId: Long): Flow<GeoMarkerDbModel?>
}