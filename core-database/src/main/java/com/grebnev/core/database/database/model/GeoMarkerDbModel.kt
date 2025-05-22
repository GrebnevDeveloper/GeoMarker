package com.grebnev.core.database.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "geo_marker")
data class GeoMarkerDbModel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
)