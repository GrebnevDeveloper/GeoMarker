package com.grebnev.feature.geomarker.presentation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.grebnev.core.extensions.componentScope
import com.grebnev.core.map.presentation.DefaultMapComponent
import com.grebnev.core.map.presentation.MapComponent
import com.grebnev.feature.bottomsheet.navigation.BottomSheetComponent
import com.grebnev.feature.bottomsheet.navigation.DefaultBottomSheetComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultGeoMarkerComponent
    @AssistedInject
    constructor(
        private val geoMarkersStoreFactory: GeoMarkersStoreFactory,
        private val mapComponentFactory: DefaultMapComponent.Factory,
        private val bottomSheetComponentFactory: DefaultBottomSheetComponent.Factory,
        @Assisted private val onAddMarkerClicked: () -> Unit,
        @Assisted componentContext: ComponentContext,
    ) : GeoMarkerComponent,
        ComponentContext by componentContext {
        private val store = instanceKeeper.getStore { geoMarkersStoreFactory.create() }

        private val _model = MutableValue(store.stateFlow.value)
        override val model: Value<GeoMarkerStore.State> = _model

        init {
            componentScope().launch {
                store.stateFlow.collect { newState ->
                    _model.value = newState
                }
            }
            componentScope().launch {
                store.labels.collect { label ->
                    when (label) {
                        GeoMarkerStore.Label.AddMarkerClicked -> onAddMarkerClicked()
                        is GeoMarkerStore.Label.MarkerDeselected -> TODO()
                        is GeoMarkerStore.Label.MarkerSelected -> TODO()
                    }
                }
            }
        }

        override val mapComponent: MapComponent =
            mapComponentFactory.create(
                componentContext = childContext("MapComponent"),
            )

        override val bottomSheetComponent: BottomSheetComponent =
            bottomSheetComponentFactory.create(
                markers = model.value.markers,
                selectedMarker = model.value.selectedMarker,
                componentContext = childContext("BottomSheetComponent"),
            )

        override fun onIntent(intent: GeoMarkerStore.Intent) = store.accept(intent)

        @AssistedFactory
        interface Factory {
            fun create(
                @Assisted onAddMarkerClicked: () -> Unit,
                @Assisted componentContext: ComponentContext,
            ): DefaultGeoMarkerComponent
        }
    }