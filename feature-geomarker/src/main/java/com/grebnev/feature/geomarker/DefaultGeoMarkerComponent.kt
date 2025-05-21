package com.grebnev.feature.geomarker

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.grebnev.core.map.presentation.DefaultMapComponent
import com.grebnev.core.map.presentation.MapComponent
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

        override fun onAddMarkerClicked() = onAddMarkerClicked.invoke()

        @AssistedFactory
        interface Factory {
            fun create(
                @Assisted onAddMarkerClicked: () -> Unit,
                @Assisted componentContext: ComponentContext,
            ): DefaultGeoMarkerComponent
        }
    }