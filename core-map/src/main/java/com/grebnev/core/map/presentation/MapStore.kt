package com.grebnev.core.map.presentation

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.grebnev.core.location.domain.GetCurrentLocationUseCase
import com.grebnev.core.location.domain.LocationState
import com.grebnev.core.location.domain.ManageLocationUpdatesUseCase
import com.grebnev.core.map.presentation.MapStore.Intent
import com.grebnev.core.map.presentation.MapStore.Label
import com.grebnev.core.map.presentation.MapStore.State
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

        data class UpdateCameraPosition(
            val position: CameraPosition,
        ) : Intent
    }

    data class State(
        val locationState: LocationState,
        val cameraPosition: CameraPosition?,
        val isFirstLocation: Boolean,
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
                Store<MapStore.Intent, MapStore.State, MapStore.Label> by storeFactory.create(
                    name = "MapStore",
                    initialState =
                        MapStore.State(
                            locationState = MapStore.State.LocationState.Initial,
                            cameraPosition = null,
                            isFirstLocation = true,
                        ),
                    bootstrapper = BootstrapperImpl(),
                    executorFactory = ::ExecutorImpl,
                    reducer = ReducerImpl,
                ) {}

        private sealed interface Action {
            data class LocationStateChanged(
                val state: MapStore.State.LocationState,
            ) : Action

            data class CameraPositionChanged(
                val position: CameraPosition,
            ) : Action
        }

        private sealed interface Msg {
            data class LocationStateChanged(
                val state: MapStore.State.LocationState,
            ) : Msg

            data class CameraPositionChanged(
                val position: CameraPosition,
            ) : Msg

            data class FirstLocationDetected(
                val point: Point,
            ) : Msg
        }

        private inner class BootstrapperImpl : CoroutineBootstrapper<Action>() {
            override fun invoke() {
                scope.launch {
                    getCurrentLocation().collect { location ->
                        val state =
                            when (location) {
                                is LocationState.Available ->
                                    MapStore.State.LocationState.Available(location.point)

                                is LocationState.Loading ->
                                    MapStore.State.LocationState.Loading

                                is LocationState.Error ->
                                    MapStore.State.LocationState.Error

                                LocationState.Initial ->
                                    MapStore.State.LocationState.Initial
                            }
                        dispatch(Action.LocationStateChanged(state))
                    }
                }
            }
        }

        private inner class ExecutorImpl :
            CoroutineExecutor<MapStore.Intent, Action, MapStore.State, Msg, MapStore.Label>() {
            private var lastCameraPosition: CameraPosition? = null

            override fun executeIntent(intent: MapStore.Intent) {
                when (intent) {
                    MapStore.Intent.StartLocationUpdates -> {
                        manageLocationUpdates.startLocationUpdates()
                    }

                    MapStore.Intent.StopLocationUpdates -> {
                        manageLocationUpdates.stopLocationUpdates()
                    }

                    MapStore.Intent.MoveToMyLocation -> {
                        val currentState = state()
                        if (currentState.locationState is MapStore.State.LocationState.Available) {
                            val point = currentState.locationState.point
                            dispatch(
                                Msg.CameraPositionChanged(
                                    CameraPosition(
                                        point,
                                        DEFAULT_ZOOM_LEVEL,
                                        0f,
                                        0f,
                                    ),
                                ),
                            )
                        }
                    }

                    is MapStore.Intent.ChangeZoom -> {
                        val currentPosition = state().cameraPosition
                        if (currentPosition != null) {
                            val newZoom =
                                (currentPosition.zoom + intent.delta).coerceIn(MIN_ZOOM..MAX_ZOOM)
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

                    is MapStore.Intent.UpdateCameraPosition -> {
                        if (lastCameraPosition != intent.position) {
                            lastCameraPosition = intent.position
                            dispatch(Msg.CameraPositionChanged(intent.position))
                        }
                    }
                }
            }

            override fun executeAction(action: Action) {
                when (action) {
                    is Action.CameraPositionChanged -> {
                    }

                    is Action.LocationStateChanged -> {
                        if (state().isFirstLocation &&
                            action.state is MapStore.State.LocationState.Available
                        ) {
                            dispatch(Msg.FirstLocationDetected(action.state.point))
                        }
                        dispatch(Msg.LocationStateChanged(action.state))
                    }
                }
            }
        }

        private object ReducerImpl : Reducer<MapStore.State, Msg> {
            override fun MapStore.State.reduce(msg: Msg): MapStore.State =
                when (msg) {
                    is Msg.LocationStateChanged ->
                        copy(locationState = msg.state)

                    is Msg.CameraPositionChanged ->
                        copy(cameraPosition = msg.position)

                    is Msg.FirstLocationDetected ->
                        copy(
                            isFirstLocation = false,
                            cameraPosition = CameraPosition(msg.point, DEFAULT_ZOOM_LEVEL, 0f, 0f),
                        )
                }
        }

        companion object {
            private const val DEFAULT_ZOOM_LEVEL = 15f
            private const val MIN_ZOOM = 1f
            private const val MAX_ZOOM = 20f
        }
    }