package com.grebnev.core.domain.entity

data class GeoMarker(
    val id: Long,
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
)