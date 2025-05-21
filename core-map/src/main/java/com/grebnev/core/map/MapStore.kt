package com.grebnev.core.map

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.grebnev.core.location.domain.GetCurrentLocationUseCase
import com.grebnev.core.location.domain.LocationState
import com.grebnev.core.location.domain.ManageLocationUpdatesUseCase
import com.grebnev.core.map.MapStore.Intent
import com.grebnev.core.map.MapStore.Label
import com.grebnev.core.map.MapStore.State
import com.grebnev.core.map.MapStore.State.LocationState.Available
import com.grebnev.core.map.MapStore.State.LocationState.Error
import com.grebnev.core.map.MapStore.State.LocationState.Initial
import com.grebnev.core.map.MapStore.State.LocationState.Loading
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import kotlinx.coroutines.launch
import javax.inject.Inject

interface MapStore : Store<Intent, State, Label> {
    sealed interface Intent {
        data object StartLocationUpdates : Intent

        data object StopLocationUpdates : Intent

        data object MoveToMyLocation : Intent

        data class ChangeZoom(
            val delta: Float,
        ) : Intent
    }

    data class State(
        val locationState: LocationState,
        val cameraPosition: CameraPosition?,
    ) {
        sealed interface LocationState {
            data object Initial : LocationState

            data object Loading : LocationState

            data class Available(
                val point: Point,
            ) : LocationState

            data object Error : LocationState
        }
    }

    sealed interface Label {
        data object PermissionDenied : Label

        data object PermissionGranted : Label
    }
}

class MapStoreFactory
    @Inject
    constructor(
        private val storeFactory: StoreFactory,
        private val manageLocationUpdates: ManageLocationUpdatesUseCase,
        private val getCurrentLocation: GetCurrentLocationUseCase,
    ) {
        fun create(): MapStore =
            object :
                MapStore,
                Store<Intent, State, Label> by storeFactory.create(
                    name = "MapStore",
                    initialState =
                        State(
                            locationState = State.LocationState.Initial,
                            cameraPosition = null,
                        ),
                    executorFactory = ::ExecutorImpl,
                    reducer = ReducerImpl,
                ) {}

        private sealed interface Action {
            data class LocationStateChanged(
                val state: State.LocationState,
            ) : Action

            data class CameraPositionChanged(
                val position: CameraPosition,
            ) : Action
        }

        private sealed interface Msg {
            data class LocationStateChanged(
                val state: State.LocationState,
            ) : Msg

            data class CameraPositionChanged(
                val position: CameraPosition,
            ) : Msg
        }

        private inner class ExecutorImpl : CoroutineExecutor<Intent, Action, State, Msg, Label>() {
            override fun executeIntent(intent: Intent) {
                when (intent) {
                    Intent.StartLocationUpdates -> {
                        scope.launch {
                            manageLocationUpdates.startLocationUpdates()
                            getCurrentLocation().collect { location ->
                                val state =
                                    when (location) {
                                        is LocationState.Available ->
                                            Available(location.point)
                                        is LocationState.Loading ->
                                            Loading
                                        is LocationState.Error ->
                                            Error
                                        LocationState.Initial ->
                                            Initial
                                    }
                                dispatch(Msg.LocationStateChanged(state))
                            }
                        }
                    }

                    Intent.StopLocationUpdates -> {
                        manageLocationUpdates.stopLocationUpdates()
                    }

                    Intent.MoveToMyLocation -> {
                        val currentState = state()
                        if (currentState.locationState is Available) {
                            val point = currentState.locationState.point
                            dispatch(Msg.CameraPositionChanged(CameraPosition(point, 15f, 0f, 0f)))
                        }
                    }

                    is Intent.ChangeZoom -> {
                        val currentPosition = state().cameraPosition
                        if (currentPosition != null) {
                            val newZoom = (currentPosition.zoom + intent.delta).coerceIn(1f..20f)
                            dispatch(
                                Msg.CameraPositionChanged(
                                    CameraPosition(
                                        currentPosition.target,
                                        newZoom,
                                        currentPosition.azimuth,
                                        currentPosition.tilt,
                                    ),
                                ),
                            )
                        }
                    }
                }
            }
        }

        private object ReducerImpl : Reducer<State, Msg> {
            override fun State.reduce(msg: Msg): State =
                when (msg) {
                    is Msg.LocationStateChanged ->
                        copy(locationState = msg.state)
                    is Msg.CameraPositionChanged ->
                        copy(cameraPosition = msg.position)
                }
        }
    }