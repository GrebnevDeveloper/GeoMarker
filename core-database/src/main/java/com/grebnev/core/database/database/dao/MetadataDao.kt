package com.grebnev.core.database.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.grebnev.core.database.database.converter.PositionConverter
import com.grebnev.core.database.database.model.MetadataDbModel
import com.yandex.mapkit.geometry.Point

@Dao
abstract class MetadataDao {
    @Suppress("ktlint:standard:function-naming")
    @Query("SELECT value FROM metadata WHERE keyMetadata=:key LIMIT 1")
    protected abstract suspend fun _getMetadataByKey(key: String): String?

    @Suppress("ktlint:standard:function-naming")
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun _updateMetadata(currentPosition: MetadataDbModel)

    suspend fun getLastPosition(): Point? {
        val metadata = _getMetadataByKey(LAST_POSITION_KEY)
        return metadata?.let { PositionConverter.fromString(metadata) }
    }

    suspend fun updateLastPosition(point: Point) {
        val metadata =
            MetadataDbModel(
                keyMetadata = LAST_POSITION_KEY,
                value = PositionConverter.toString(point),
            )
        _updateMetadata(metadata)
    }

    companion object {
        private const val LAST_POSITION_KEY = "last_position"
    }
}