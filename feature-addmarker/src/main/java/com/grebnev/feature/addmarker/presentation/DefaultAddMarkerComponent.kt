package com.grebnev.feature.addmarker.presentation

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
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultAddMarkerComponent
    @AssistedInject
    constructor(
        private val addMarkerStoreFactory: AddMarkerStoreFactory,
        private val mapComponentFactory: DefaultMapComponent.LocationPickerFactory,
        @Assisted private val onBackClicked: () -> Unit,
        @Assisted componentContext: ComponentContext,
    ) : AddMarkerComponent,
        ComponentContext by componentContext {
        private val store = instanceKeeper.getStore { addMarkerStoreFactory.create() }

        private val _model = MutableValue(store.stateFlow.value)
        override val model: Value<AddMarkerStore.State> = _model

        init {
            componentScope().launch {
                store.stateFlow.collect { newState ->
                    _model.value = newState
                }
            }
            componentScope().launch {
                store.labels.collect { label ->
                    when (label) {
                        AddMarkerStore.Label.MarkerAdded -> onBackClicked()
                        AddMarkerStore.Label.BackClicked -> onBackClicked()
                    }
                }
            }
        }

        override val mapComponent: MapComponent =
            mapComponentFactory.create(
                cameraPositionChanged = { position ->
                    store.accept(AddMarkerStore.Intent.CameraPositionChanged(position))
                },
                componentContext = childContext("MapComponent"),
            )

        override fun onIntent(intent: AddMarkerStore.Intent) {
            store.accept(intent)
        }

        @AssistedFactory
        interface Factory {
            fun create(
                @Assisted onBackClicked: () -> Unit,
                @Assisted componentContext: ComponentContext,
            ): DefaultAddMarkerComponent
        }
    }