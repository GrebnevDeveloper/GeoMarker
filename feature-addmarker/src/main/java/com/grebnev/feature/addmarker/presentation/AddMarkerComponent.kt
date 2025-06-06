package com.grebnev.feature.addmarker.presentation

import com.arkivanov.decompose.value.Value
import com.grebnev.core.map.presentation.MapComponent
import com.grebnev.feature.imagepicker.presentation.ImagePickerComponent

interface AddMarkerComponent {
    val model: Value<AddMarkerStore.State>

    val mapComponent: MapComponent

    val imagePickerComponent: ImagePickerComponent

    fun onIntent(intent: AddMarkerStore.Intent)
}