package com.grebnev.core.map.presentation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.grebnev.core.extensions.componentScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultMapComponent
    @AssistedInject
    constructor(
        private val mapStoreFactory: MapStoreFactory,
        @Assisted componentContext: ComponentContext,
    ) : MapComponent,
        ComponentContext by componentContext {
        private val store = instanceKeeper.getStore { mapStoreFactory.create() }

        private val _model = MutableValue(store.stateFlow.value)
        override val model: Value<MapStore.State> = _model

        init {
            componentScope().launch {
                store.stateFlow.collect { newState ->
                    _model.value = newState
                }
            }
        }

        override fun onIntent(intent: MapStore.Intent) {
            store.accept(intent)
        }

        @AssistedFactory
        interface Factory {
            fun create(
                @Assisted componentContext: ComponentContext,
            ): DefaultMapComponent
        }
    }