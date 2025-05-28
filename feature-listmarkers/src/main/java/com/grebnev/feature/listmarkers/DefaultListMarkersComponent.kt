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
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class DefaultListMarkersComponent
    @AssistedInject
    constructor(
        private val listMarkersStoreFactory: ListMarkersStoreFactory,
        @Assisted private val markersFlow: Flow<List<GeoMarker>>,
        @Assisted private val onMarkerSelected: (Long) -> Unit,
        @Assisted component: ComponentContext,
    ) : ListMarkersComponent,
        ComponentContext by component {
        private val store = instanceKeeper.getStore { listMarkersStoreFactory.create(markersFlow) }

        private val _model = MutableValue(store.state)
        override val model: Value<ListMarkersStore.State> = _model

        init {
            componentScope().launch {
                store.stateFlow.collect { newState ->
                    _model.value = newState
                }
            }
            componentScope().launch {
                store.labels.collect { label ->
                    when (label) {
                        is ListMarkersStore.Label.MarkerClicked -> onMarkerSelected(label.markerId)
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
                @Assisted markersFlow: Flow<List<GeoMarker>>,
                @Assisted onMarkerSelected: (Long) -> Unit,
                @Assisted component: ComponentContext,
            ): DefaultListMarkersComponent
        }
    }