package com.grebnev.feature.geomarker.presentation

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.grebnev.core.domain.entity.GeoMarker
import com.grebnev.feature.geomarker.domain.GetGeoMarkersUseCase
import com.grebnev.feature.geomarker.presentation.GeoMarkerStore.Intent
import com.grebnev.feature.geomarker.presentation.GeoMarkerStore.Label
import com.grebnev.feature.geomarker.presentation.GeoMarkerStore.State
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject

interface GeoMarkerStore : Store<Intent, State, Label> {
    sealed interface Intent {
        data class SelectMarker(
            val markerId: Long,
        ) : Intent

        data class DeselectMarker(
            val markerId: Long,
        ) : Intent

        data object AddMarkerClicked : Intent
    }

    data class State(
        val markersFlow: Flow<List<GeoMarker>> = emptyFlow(),
        val selectedMarker: GeoMarker? = null,
    )

    sealed interface Label {
        data object AddMarkerClicked : Label

        data class MarkerSelected(
            val markerId: Long,
        ) : Label

        data class MarkerDeselected(
            val markerId: Long,
        ) : Label
    }
}

class GeoMarkersStoreFactory
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
                        initialState = State(),
                        bootstrapper = BootstrapperImpl(),
                        executorFactory = ::ExecutorImpl,
                        reducer = ReducerImpl,
                    ) {}

        private sealed interface Action {
            data class MarkersLoaded(
                val markersFlow: Flow<List<GeoMarker>>,
            ) : Action
        }

        private sealed interface Msg {
            data class MarkersLoaded(
                val markersFlow: Flow<List<GeoMarker>>,
            ) : Msg

            data class MarkerSelected(
                val markerId: Long,
            ) : Msg

            data class MarkerDeselected(
                val markerId: Long,
            ) : Msg
        }

        private inner class BootstrapperImpl : CoroutineBootstrapper<Action>() {
            override fun invoke() {
                val markersFlow = getGeoMarkersUseCase()
                dispatch(Action.MarkersLoaded(markersFlow))
            }
        }

        private inner class ExecutorImpl : CoroutineExecutor<Intent, Action, State, Msg, Label>() {
            override fun executeIntent(intent: Intent) {
                when (intent) {
                    is Intent.SelectMarker -> {
                        dispatch(Msg.MarkerSelected(intent.markerId))
                        publish(Label.MarkerSelected(intent.markerId))
                    }

                    is Intent.DeselectMarker -> {
                        dispatch(Msg.MarkerDeselected(intent.markerId))
                        publish(Label.MarkerDeselected(intent.markerId))
                    }

                    is Intent.AddMarkerClicked -> {
                        publish(Label.AddMarkerClicked)
                    }
                }
            }

            override fun executeAction(action: Action) {
                when (action) {
                    is Action.MarkersLoaded -> {
                        dispatch(Msg.MarkersLoaded(action.markersFlow))
                    }
                }
            }
        }

        private object ReducerImpl : Reducer<State, Msg> {
            override fun State.reduce(msg: Msg): State =
                when (msg) {
                    is Msg.MarkersLoaded ->
                        copy(
                            markersFlow = msg.markersFlow,
                        )

                    is Msg.MarkerSelected ->
                        copy(
                            selectedMarker = null,
                        )

                    is Msg.MarkerDeselected ->
                        copy(
                            selectedMarker = null,
                        )
                }
        }
    }