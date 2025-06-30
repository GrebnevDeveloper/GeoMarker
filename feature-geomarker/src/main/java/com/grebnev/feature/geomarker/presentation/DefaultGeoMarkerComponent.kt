package com.grebnev.feature.geomarker.presentation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.grebnev.core.common.delegates.StateFlowDelegate
import com.grebnev.core.common.extensions.scope
import com.grebnev.core.domain.entity.GeoMarker
import com.grebnev.core.map.presentation.DefaultMapComponentProvider
import com.grebnev.core.map.presentation.MapComponent
import com.grebnev.feature.bottomsheet.navigation.BottomSheetComponent
import com.grebnev.feature.bottomsheet.navigation.DefaultBottomSheetComponent
import com.grebnev.feature.geomarker.api.GeoMarkerStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultGeoMarkerComponent
    @AssistedInject
    constructor(
        private val geoMarkerStoreFactory: GeoMarkerStoreFactory,
        private val mapComponentProvider: DefaultMapComponentProvider,
        private val bottomSheetComponentFactory: DefaultBottomSheetComponent.Factory,
        @Assisted private val onAddMarkerClicked: () -> Unit,
        @Assisted private val onEditMarkerClicked: (GeoMarker) -> Unit,
        @Assisted componentContext: ComponentContext,
    ) : GeoMarkerComponent,
        ComponentContext by componentContext {
        private val store = instanceKeeper.getStore { geoMarkerStoreFactory.create() }

        override val model: Value<GeoMarkerStore.State> by StateFlowDelegate(scope, store.stateFlow)

        init {
            scope.launch {
                store.labels.collect { label ->
                    when (label) {
                        GeoMarkerStore.Label.AddMarkerClicked -> onAddMarkerClicked()
                    }
                }
            }
        }

        override val mapComponent: MapComponent =
            mapComponentProvider.createMapMarkers(
                geoMarkerStore = store,
                onMarkerSelected = { marker -> store.accept(GeoMarkerStore.Intent.SelectMarker(marker)) },
                componentContext = childContext("MapComponent"),
            )

        override val bottomSheetComponent: BottomSheetComponent =
            bottomSheetComponentFactory.create(
                geoMarkerStore = store,
                onMarkerSelected = { marker -> store.accept(GeoMarkerStore.Intent.SelectMarker(marker)) },
                onEditMarkerClicked = { marker -> onEditMarkerClicked(marker) },
                componentContext = childContext("BottomSheetComponent"),
            )

        override fun onIntent(intent: GeoMarkerStore.Intent) = store.accept(intent)

        @AssistedFactory
        interface Factory {
            fun create(
                @Assisted onAddMarkerClicked: () -> Unit,
                @Assisted onEditMarkerClicked: (GeoMarker) -> Unit,
                @Assisted componentContext: ComponentContext,
            ): DefaultGeoMarkerComponent
        }
    }