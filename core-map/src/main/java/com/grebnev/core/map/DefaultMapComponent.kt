package com.grebnev.core.map

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi

class DefaultMapComponent
    @AssistedInject
    constructor(
        private val mapStoreFactory: MapStoreFactory,
        @Assisted componentContext: ComponentContext,
    ) : MapComponent,
        ComponentContext by componentContext {
        private val store = instanceKeeper.getStore { mapStoreFactory.create() }

        @OptIn(ExperimentalCoroutinesApi::class)
        override fun onStartUpdates() {
            store.accept(MapStore.Intent.StartLocationUpdates)
        }

        override fun onStopUpdates() {
            store.accept(MapStore.Intent.StopLocationUpdates)
        }

        override fun onMoveToMyLocation() {
            store.accept(MapStore.Intent.MoveToMyLocation)
        }

        override fun onChangeZoom(delta: Float) {
            store.accept(MapStore.Intent.ChangeZoom(delta))
        }

        @AssistedFactory
        interface Factory {
            fun create(
                @Assisted componentContext: ComponentContext,
            ): DefaultMapComponent
        }
    }