package com.grebnev.feature.geomarker

import com.grebnev.core.map.presentation.MapComponent

interface GeoMarkerComponent {
    val mapComponent: MapComponent

    fun onAddMarkerClicked()
}