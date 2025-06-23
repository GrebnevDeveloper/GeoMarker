package com.grebnev.core.database.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.grebnev.core.database.database.model.MetadataDbModel
import kotlinx.coroutines.flow.Flow

@Dao
interface MetadataDao {
    @Query("SELECT value FROM metadata WHERE keyMetadata=:key LIMIT 1")
    suspend fun getMetadataByKey(key: String): String?

    @Query("SELECT value FROM metadata WHERE keyMetadata=:key LIMIT 1")
    fun getMetadataFlowByKey(key: String): Flow<String?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateMetadata(currentPosition: MetadataDbModel)
}