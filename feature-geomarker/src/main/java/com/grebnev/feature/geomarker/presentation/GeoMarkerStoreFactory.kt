package com.grebnev.feature.geomarker.presentation

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.grebnev.core.domain.entity.GeoMarker
import com.grebnev.feature.geomarker.api.GeoMarkerStore
import com.grebnev.feature.geomarker.api.GeoMarkerStore.Intent
import com.grebnev.feature.geomarker.api.GeoMarkerStore.Label
import com.grebnev.feature.geomarker.api.GeoMarkerStore.State
import com.grebnev.feature.geomarker.domain.GetGeoMarkersUseCase
import kotlinx.coroutines.launch
import javax.inject.Inject

class GeoMarkerStoreFactory
    @Inject
    constructor(
        private val storeFactory: StoreFactory,
        private val getGeoMarkersUseCase: GetGeoMarkersUseCase,
    ) {
        fun create(): GeoMarkerStore =
            object :
                GeoMarkerStore,
                Store<Intent, State, Label> by storeFactory
                    .create(
                        name = "GeoMarkersStore",
                        initialState =
                            State(
                                markers = emptyList(),
                                selectedMarkerId = null,
                            ),
                        bootstrapper = BootstrapperImpl(),
                        executorFactory = ::ExecutorImpl,
                        reducer = ReducerImpl,
                    ) {}

        private sealed interface Action {
            data class MarkersLoaded(
                val markers: List<GeoMarker>,
            ) : Action
        }

        private sealed interface Msg {
            data class MarkersLoaded(
                val markers: List<GeoMarker>,
            ) : Msg

            data class MarkerSelected(
                val markerId: Long?,
            ) : Msg
        }

        private inner class BootstrapperImpl : CoroutineBootstrapper<Action>() {
            override fun invoke() {
                scope.launch {
                    getGeoMarkersUseCase().collect { markers ->
                        dispatch(Action.MarkersLoaded(markers))
                    }
                }
            }
        }

        private inner class ExecutorImpl : CoroutineExecutor<Intent, Action, State, Msg, Label>() {
            override fun executeIntent(intent: Intent) {
                when (intent) {
                    is Intent.SelectMarker -> {
                        dispatch(Msg.MarkerSelected(intent.markerId))
                    }

                    is Intent.AddMarkerClicked -> {
                        publish(Label.AddMarkerClicked)
                    }
                }
            }

            override fun executeAction(action: Action) {
                when (action) {
                    is Action.MarkersLoaded -> {
                        dispatch(Msg.MarkersLoaded(action.markers))
                    }
                }
            }
        }

        private object ReducerImpl : Reducer<State, Msg> {
            override fun State.reduce(msg: Msg): State =
                when (msg) {
                    is Msg.MarkersLoaded ->
                        copy(markers = msg.markers)

                    is Msg.MarkerSelected -> {
                        copy(selectedMarkerId = msg.markerId)
                    }
                }
        }
    }