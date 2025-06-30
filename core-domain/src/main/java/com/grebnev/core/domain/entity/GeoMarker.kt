package com.grebnev.core.domain.entity

import kotlinx.serialization.Serializable

@Serializable
data class GeoMarker(
    val id: Long = 0,
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val imagesUri: List<String>,
)