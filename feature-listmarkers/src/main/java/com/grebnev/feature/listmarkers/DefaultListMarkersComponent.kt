@file:OptIn(ExperimentalCoroutinesApi::class)

package com.grebnev.feature.listmarkers

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.grebnev.core.domain.entity.GeoMarker
import com.grebnev.core.extensions.componentScope
import com.grebnev.feature.geomarker.api.GeoMarkerStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

class DefaultListMarkersComponent
    @AssistedInject
    constructor(
        private val listMarkersStoreFactory: ListMarkersStoreFactory,
        @Assisted geoMarkerStore: GeoMarkerStore,
        @Assisted private val onMarkerSelected: (GeoMarker) -> Unit,
        @Assisted component: ComponentContext,
    ) : ListMarkersComponent,
        ComponentContext by component {
        private val store = instanceKeeper.getStore { listMarkersStoreFactory.create(geoMarkerStore) }

        private val _model = MutableValue(store.state)
        override val model: Value<ListMarkersStore.State> = _model

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
                        is ListMarkersStore.Label.MarkerClicked -> onMarkerSelected(label.marker)
                    }
                }
            }
        }

        override fun onIntent(intent: ListMarkersStore.Intent) {
            store.accept(intent)
        }

        @AssistedFactory
        interface Factory {
            fun create(
                @Assisted geoMarkerStore: GeoMarkerStore,
                @Assisted onMarkerSelected: (GeoMarker) -> Unit,
                @Assisted component: ComponentContext,
            ): DefaultListMarkersComponent
        }
    }