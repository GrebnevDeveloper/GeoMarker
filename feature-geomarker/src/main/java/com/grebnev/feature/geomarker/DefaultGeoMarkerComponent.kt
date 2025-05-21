package com.grebnev.feature.geomarker

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.grebnev.core.map.DefaultMapComponent
import com.grebnev.core.map.MapComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class DefaultGeoMarkerComponent
    @AssistedInject
    constructor(
        private val mapComponentFactory: DefaultMapComponent.Factory,
        @Assisted private val onAddMarkerClicked: () -> Unit,
        @Assisted componentContext: ComponentContext,
    ) : GeoMarkerComponent,
        ComponentContext by componentContext {
        override val mapComponent: MapComponent =
            mapComponentFactory.create(
                componentContext = childContext("MapComponent"),
            )

        override fun onAddMarkerClicked(): Unit = onAddMarkerClicked()

        @AssistedFactory
        interface Factory {
            fun create(
                @Assisted onAddMarkerClicked: () -> Unit,
                @Assisted componentContext: ComponentContext,
            ): DefaultGeoMarkerComponent
        }
    }