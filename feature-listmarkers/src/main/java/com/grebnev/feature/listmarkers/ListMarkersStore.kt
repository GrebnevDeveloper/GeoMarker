package com.grebnev.feature.listmarkers

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.grebnev.core.domain.entity.GeoMarker
import com.grebnev.feature.geomarker.api.GeoMarkerStore
import com.grebnev.feature.listmarkers.ListMarkersStore.Intent
import com.grebnev.feature.listmarkers.ListMarkersStore.Label
import com.grebnev.feature.listmarkers.ListMarkersStore.State
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

interface ListMarkersStore : Store<Intent, State, Label> {
    sealed interface Intent {
        data class MarkerClicked(
            val marker: GeoMarker,
        ) : Intent
    }

    data class State(
        val markers: List<GeoMarker>,
    )

    sealed interface Label {
        data class MarkerClicked(
            val marker: GeoMarker,
        ) : Label
    }
}

class ListMarkersStoreFactory
    @Inject
    constructor(
        private val storeFactory: StoreFactory,
    ) {
        fun create(geoMarkerStore: GeoMarkerStore): ListMarkersStore =
            object :
                ListMarkersStore,
                Store<Intent, State, Label> by storeFactory.create(
                    name = "ListMarkersStore",
                    initialState =
                        State(
                            markers = emptyList(),
                        ),
                    bootstrapper = BootstrapperImpl(geoMarkerStore),
                    executorFactory = ::ExecutorImpl,
                    reducer = ReducerImpl,
                ) {}

        private sealed interface Action {
            data class MarkersUpdated(
                val markers: List<GeoMarker>,
            ) : Action
        }

        private sealed interface Msg {
            data class MarkersLoaded(
                val markers: List<GeoMarker>,
            ) : Msg
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private inner class BootstrapperImpl(
            private val geoMarkerStore: GeoMarkerStore,
        ) : CoroutineBootstrapper<Action>() {
            override fun invoke() {
                scope.launch {
                    geoMarkerStore.stateFlow.collect { state ->
                        dispatch(Action.MarkersUpdated(state.markers))
                    }
                }
            }
        }

        private inner class ExecutorImpl : CoroutineExecutor<Intent, Action, State, Msg, Label>() {
            override fun executeIntent(intent: Intent) {
                when (intent) {
                    is Intent.MarkerClicked -> {
                        publish(Label.MarkerClicked(intent.marker))
                    }
                }
            }

            override fun executeAction(action: Action) {
                when (action) {
                    is Action.MarkersUpdated -> {
                        dispatch(Msg.MarkersLoaded(action.markers))
                    }
                }
            }
        }

        private object ReducerImpl : Reducer<State, Msg> {
            override fun State.reduce(msg: Msg): State =
                when (msg) {
                    is Msg.MarkersLoaded ->
                        copy(
                            markers = msg.markers,
                        )
                }
        }
    }