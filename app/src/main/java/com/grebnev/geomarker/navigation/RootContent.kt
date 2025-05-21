package com.grebnev.geomarker.navigation

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.grebnev.feature.geomarker.GeoMarkerContent
import com.grebnev.geomarker.ui.theme.GeoMarkerTheme

@Composable
fun RootContent(component: RootComponent) {
    GeoMarkerTheme {
        Children(stack = component.stack) {
            when (val instance = it.instance) {
                is RootComponent.Child.GeoMarker -> {
                    GeoMarkerContent(component = instance.geoMarkerComponent)
                }
            }
        }
    }
}