package com.grebnev.core.database.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.grebnev.core.database.database.converter.PositionConverter

@Entity(tableName = "metadata")
@TypeConverters(PositionConverter::class)
data class MetadataDbModel(
    @PrimaryKey val keyMetadata: String,
    val value: String,
)