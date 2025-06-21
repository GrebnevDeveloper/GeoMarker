package com.grebnev.geomarker.navigation

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.grebnev.feature.editormarker.presentation.AddMarkerContent
import com.grebnev.feature.geomarker.presentation.GeoMarkerContent
import com.grebnev.geomarker.ui.theme.GeoMarkerTheme

@Composable
fun RootContent(component: RootComponent) {
    GeoMarkerTheme {
        Children(stack = component.stack) {
            when (val instance = it.instance) {
                is RootComponent.Child.GeoMarkers -> {
                    GeoMarkerContent(component = instance.geoMarkerComponent)
                }
                is RootComponent.Child.AddMarker -> {
                    AddMarkerContent(component = instance.editorMarkerComponent)
                }

                is RootComponent.Child.EditMarker -> {
                    AddMarkerContent(component = instance.editorMarkerComponent)
                }
            }
        }
    }
}