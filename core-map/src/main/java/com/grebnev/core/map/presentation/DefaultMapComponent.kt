package com.grebnev.core.map.presentation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.grebnev.core.domain.entity.GeoMarker
import com.grebnev.core.extensions.componentScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultMapComponent
    @AssistedInject
    constructor(
        private val mapStoreFactory: MapStoreFactory,
        @Assisted markersFlow: Flow<List<GeoMarker>>,
        @Assisted selectedMarkerIdFlow: StateFlow<Long?>,
        @Assisted private val onMarkerSelected: (Long?) -> Unit,
        @Assisted componentContext: ComponentContext,
    ) : MapComponent,
        ComponentContext by componentContext {
        private val store =
            instanceKeeper.getStore {
                mapStoreFactory.create(
                    markersFlow = markersFlow,
                    selectedMarkerIdFlow = selectedMarkerIdFlow,
                )
            }

        private val _model = MutableValue(store.stateFlow.value)
        override val model: Value<MapStore.State> = _model

        private val scope = componentScope()

        init {
            scope.launch {
                store.stateFlow.collect { newState ->
                    _model.value = newState
                }
            }
            scope.launch {
                store.labels.collect { label ->
                    when (label) {
                        is MapStore.Label.MarkerSelected ->
                            onMarkerSelected(label.markerId)
                    }
                }
            }
        }

        override fun onIntent(intent: MapStore.Intent) {
            store.accept(intent)
        }

        @AssistedFactory
        interface Factory {
            fun create(
                @Assisted markersFlow: Flow<List<GeoMarker>>,
                @Assisted selectedMarkerIdFlow: StateFlow<Long?>,
                @Assisted onMarkerSelected: (Long?) -> Unit,
                @Assisted componentContext: ComponentContext,
            ): DefaultMapComponent
        }
    }