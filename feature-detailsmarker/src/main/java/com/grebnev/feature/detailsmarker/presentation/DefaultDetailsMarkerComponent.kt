@file:OptIn(ExperimentalCoroutinesApi::class)

package com.grebnev.feature.detailsmarker.presentation

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
import kotlinx.coroutines.launch

class DefaultDetailsMarkerComponent
    @AssistedInject
    constructor(
        private val detailsStoreFactory: DetailsMarkerStoreFactory,
        @Assisted private val marker: GeoMarker,
        @Assisted private val onBackClicked: () -> Unit,
        @Assisted private val onEditClicked: (GeoMarker) -> Unit,
        @Assisted component: ComponentContext,
    ) : DetailsMarkerComponent,
        ComponentContext by component {
        private val store = instanceKeeper.getStore { detailsStoreFactory.create(marker) }

        private val _model = MutableValue(store.state)
        override val model: Value<DetailsMarkerStore.State> = _model

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
                        DetailsMarkerStore.Label.BackClicked -> {
                            onBackClicked()
                        }

                        is DetailsMarkerStore.Label.EditClicked ->
                            onEditClicked(label.geoMarker)
                    }
                }
            }
        }

        override fun onIntent(intent: DetailsMarkerStore.Intent) {
            store.accept(intent)
        }

        @AssistedFactory
        interface Factory {
            fun create(
                @Assisted marker: GeoMarker,
                @Assisted onBackClicked: () -> Unit,
                @Assisted onEditClicked: (GeoMarker) -> Unit,
                @Assisted component: ComponentContext,
            ): DefaultDetailsMarkerComponent
        }
    }