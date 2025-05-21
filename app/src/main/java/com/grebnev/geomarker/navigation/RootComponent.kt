package com.grebnev.geomarker.navigation

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.grebnev.feature.addmarker.AddMarkerComponent
import com.grebnev.feature.geomarker.GeoMarkerComponent

interface RootComponent {
    val stack: Value<ChildStack<*, Child>>

    sealed interface Child {
        data class GeoMarker(
            val geoMarkerComponent: GeoMarkerComponent,
        ) : Child

        data class AddMarker(
            val addMarkerComponent: AddMarkerComponent,
        ) : Child
    }
}