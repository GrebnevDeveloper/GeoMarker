package com.grebnev.feature.geomarker

import com.grebnev.core.map.MapComponent

interface GeoMarkerComponent {
    val mapComponent: MapComponent

    fun onAddMarkerClicked()
}