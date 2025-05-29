package com.grebnev.feature.geomarker.presentation

import com.arkivanov.decompose.value.Value
import com.grebnev.core.map.presentation.MapComponent
import com.grebnev.feature.bottomsheet.navigation.BottomSheetComponent

interface GeoMarkerComponent {
    val model: Value<GeoMarkerStore.State>

    val mapComponent: MapComponent
    val bottomSheetComponent: BottomSheetComponent

    fun onIntent(intent: GeoMarkerStore.Intent)
}