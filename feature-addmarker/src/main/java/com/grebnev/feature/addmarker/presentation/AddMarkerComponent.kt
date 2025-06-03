package com.grebnev.feature.addmarker.presentation

import com.arkivanov.decompose.value.Value
import com.grebnev.core.map.presentation.MapComponent

interface AddMarkerComponent {
    val model: Value<AddMarkerStore.State>

    val mapComponent: MapComponent

    fun onIntent(intent: AddMarkerStore.Intent)
}