package com.grebnev.feature.detailsmarker.presentation

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.grebnev.core.common.wrappers.Result
import com.grebnev.core.domain.entity.GeoMarker
import com.grebnev.feature.detailsmarker.domain.GetDetailsMarkerUseCase
import com.grebnev.feature.detailsmarker.presentation.DetailsMarkerStore.Intent
import com.grebnev.feature.detailsmarker.presentation.DetailsMarkerStore.Label
import com.grebnev.feature.detailsmarker.presentation.DetailsMarkerStore.State
import kotlinx.coroutines.launch
import javax.inject.Inject

interface DetailsMarkerStore : Store<Intent, State, Label> {
    sealed interface Intent {
        data object BackClicked : Intent

        data class EditMarkerClicked(
            val geoMarker: GeoMarker,
        ) : Intent
    }

    data class State(
        val markerResult: Result<GeoMarker>,
    )

    sealed interface Label {
        data object BackClicked : Label

        data class EditClicked(
            val geoMarker: GeoMarker,
        ) : Label
    }
}

class DetailsMarkerStoreFactory
    @Inject
    constructor(
        private val storeFactory: StoreFactory,
        private val getDetailsMarkerUseCase: GetDetailsMarkerUseCase,
    ) {
        fun create(marker: GeoMarker): DetailsMarkerStore =
            object :
                DetailsMarkerStore,
                Store<Intent, State, Label> by storeFactory
                    .create(
                        name = "DetailsMarkerStore",
                        initialState = State(Result.success(marker)),
                        bootstrapper = BootstrapperImpl(marker.id),
                        executorFactory = ::ExecutorImpl,
                        reducer = ReducerImpl,
                    ) {}

        private sealed interface Action {
            data class MarkerLoaded(
                val markerResult: Result<GeoMarker>,
            ) : Action
        }

        private sealed interface Msg {
            data class MarkerLoaded(
                val markerResult: Result<GeoMarker>,
            ) : Msg
        }

        private inner class BootstrapperImpl(
            private val markerId: Long,
        ) : CoroutineBootstrapper<Action>() {
            override fun invoke() {
                scope.launch {
                    getDetailsMarkerUseCase(markerId).collect { marker ->
                        dispatch(Action.MarkerLoaded(marker))
                    }
                }
            }
        }

        private inner class ExecutorImpl : CoroutineExecutor<Intent, Action, State, Msg, Label>() {
            override fun executeIntent(intent: Intent) {
                when (intent) {
                    Intent.BackClicked ->
                        publish(Label.BackClicked)

                    is Intent.EditMarkerClicked ->
                        publish(Label.EditClicked(intent.geoMarker))
                }
            }

            override fun executeAction(action: Action) {
                when (action) {
                    is Action.MarkerLoaded ->
                        dispatch(Msg.MarkerLoaded(action.markerResult))
                }
            }
        }

        private object ReducerImpl : Reducer<State, Msg> {
            override fun State.reduce(msg: Msg): State =
                when (msg) {
                    is Msg.MarkerLoaded -> {
                        copy(markerResult = msg.markerResult)
                    }
                }
        }
    }