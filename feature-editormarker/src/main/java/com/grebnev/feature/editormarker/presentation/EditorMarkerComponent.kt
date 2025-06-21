package com.grebnev.feature.editormarker.presentation

import com.arkivanov.decompose.value.Value
import com.grebnev.core.map.presentation.MapComponent
import com.grebnev.feature.imagepicker.presentation.ImagePickerComponent

interface EditorMarkerComponent {
    val model: Value<EditorMarkerStore.State>

    val mapComponent: MapComponent

    val imagePickerComponent: ImagePickerComponent

    fun onIntent(intent: EditorMarkerStore.Intent)
}