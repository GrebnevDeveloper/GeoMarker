package com.grebnev.core.database.mapper

import com.grebnev.core.database.database.model.GeoMarkerDbModel
import com.grebnev.core.domain.entity.GeoMarker

fun GeoMarker.toGeoMarkerDbModel(): GeoMarkerDbModel =
    GeoMarkerDbModel(
        title = title,
        description = description,
        latitude = latitude,
        longitude = longitude,
        imagesUri = imagesUri,
    )

fun GeoMarkerDbModel.toGeoMarker(): GeoMarker =
    GeoMarker(id, title, description, latitude, longitude, imagesUri)

fun List<GeoMarkerDbModel>.toGeoMarkers(): List<GeoMarker> = map { it.toGeoMarker() }