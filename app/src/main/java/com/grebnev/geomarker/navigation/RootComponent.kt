package com.grebnev.geomarker.navigation

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.grebnev.feature.editormarker.presentation.EditorMarkerComponent
import com.grebnev.feature.geomarker.presentation.GeoMarkerComponent

interface RootComponent {
    val stack: Value<ChildStack<*, Child>>

    sealed interface Child {
        data class GeoMarkers(
            val geoMarkerComponent: GeoMarkerComponent,
        ) : Child

        data class AddMarker(
            val editorMarkerComponent: EditorMarkerComponent,
        ) : Child

        data class EditMarker(
            val editorMarkerComponent: EditorMarkerComponent,
        ) : Child
    }
}