package com.grebnev.feature.geomarker.presentation

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.grebnev.core.common.wrappers.Result
import com.grebnev.core.domain.entity.GeoMarker
import com.grebnev.feature.geomarker.api.GeoMarkerStore
import com.grebnev.feature.geomarker.api.GeoMarkerStore.Intent
import com.grebnev.feature.geomarker.api.GeoMarkerStore.Label
import com.grebnev.feature.geomarker.api.GeoMarkerStore.State
import com.grebnev.feature.geomarker.domain.GetNearestGeoMarkersUseCase
import kotlinx.coroutines.launch
import javax.inject.Inject

class GeoMarkerStoreFactory
    @Inject
    constructor(
        private val storeFactory: StoreFactory,
        private val getNearestGeoMarkersUseCase: GetNearestGeoMarkersUseCase,
    ) {
        fun create(): GeoMarkerStore =
            object :
                GeoMarkerStore,
                Store<Intent, State, Label> by storeFactory
                    .create(
                        name = "GeoMarkersStore",
                        initialState =
                            State(
                                markersResult = Result.loading(),
                                selectedMarker = null,
                            ),
                        bootstrapper = BootstrapperImpl(),
                        executorFactory = ::ExecutorImpl,
                        reducer = ReducerImpl,
                    ) {}

        private sealed interface Action {
            data class MarkersLoaded(
                val markersResult: Result<List<GeoMarker>>,
            ) : Action
        }

        private sealed interface Msg {
            data class MarkersLoaded(
                val markersResult: Result<List<GeoMarker>>,
            ) : Msg

            data class MarkerSelected(
                val marker: GeoMarker?,
            ) : Msg
        }

        private inner class BootstrapperImpl : CoroutineBootstrapper<Action>() {
            override fun invoke() {
                scope.launch {
                    getNearestGeoMarkersUseCase().collect { markers ->
                        dispatch(Action.MarkersLoaded(markers))
                    }
                }
            }
        }

        private inner class ExecutorImpl : CoroutineExecutor<Intent, Action, State, Msg, Label>() {
            override fun executeIntent(intent: Intent) {
                when (intent) {
                    is Intent.SelectMarker -> {
                        dispatch(Msg.MarkerSelected(intent.marker))
                    }

                    is Intent.AddMarkerClicked -> {
                        publish(Label.AddMarkerClicked)
                    }
                }
            }

            override fun executeAction(action: Action) {
                when (action) {
                    is Action.MarkersLoaded -> {
                        dispatch(Msg.MarkersLoaded(action.markersResult))
                    }
                }
            }
        }

        private object ReducerImpl : Reducer<State, Msg> {
            override fun State.reduce(msg: Msg): State =
                when (msg) {
                    is Msg.MarkersLoaded ->
                        copy(markersResult = msg.markersResult)

                    is Msg.MarkerSelected -> {
                        copy(selectedMarker = msg.marker)
                    }
                }
        }
    }