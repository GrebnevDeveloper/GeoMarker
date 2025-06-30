package com.grebnev.core.database.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.grebnev.core.database.database.converter.ImagesUriConverter

@Entity(tableName = "geo_marker")
@TypeConverters(ImagesUriConverter::class)
data class GeoMarkerDbModel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val imagesUri: List<String>,
)