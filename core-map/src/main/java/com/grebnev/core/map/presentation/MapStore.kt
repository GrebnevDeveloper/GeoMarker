package com.grebnev.core.map.presentation

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.grebnev.core.domain.entity.GeoMarker
import com.grebnev.core.location.domain.entity.LocationStatus
import com.grebnev.core.location.domain.usecase.GetCurrentLocationUseCase
import com.grebnev.core.location.domain.usecase.ManageLocationUpdatesUseCase
import com.grebnev.core.map.presentation.MapStore.Intent
import com.grebnev.core.map.presentation.MapStore.Label
import com.grebnev.core.map.presentation.MapStore.State
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
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

        data class MarkerClicked(
            val markerId: Long,
        ) : Intent
    }

    data class State(
        val locationState: LocationState,
        val cameraPosition: CameraPosition?,
        val isFirstLocation: Boolean,
        val timeUpdate: Long,
        val markers: List<GeoMarker>,
        val selectedMarkerId: Long?,
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
        data class MarkerSelected(
            val markerId: Long?,
        ) : Label
    }
}

class MapStoreFactory
    @Inject
    constructor(
        private val storeFactory: StoreFactory,
        private val manageLocationUpdates: ManageLocationUpdatesUseCase,
        private val getCurrentLocation: GetCurrentLocationUseCase,
    ) {
        fun create(
            markersFlow: Flow<List<GeoMarker>>,
            selectedMarkerIdFlow: StateFlow<Long?>,
        ): MapStore =
            object :
                MapStore,
                Store<Intent, State, Label> by storeFactory.create(
                    name = "MapStore",
                    initialState =
                        State(
                            locationState = State.LocationState.Initial,
                            cameraPosition = null,
                            isFirstLocation = true,
                            timeUpdate = 0L,
                            markers = emptyList(),
                            selectedMarkerId = null,
                        ),
                    bootstrapper = BootstrapperImpl(markersFlow, selectedMarkerIdFlow),
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

            data class TimeUpdateChanged(
                val timeUpdate: Long,
            ) : Action

            data class MarkersUpdated(
                val markers: List<GeoMarker>,
            ) : Action

            data class MarkerSelected(
                val markerId: Long?,
            ) : Action
        }

        private sealed interface Msg {
            data class LocationStateChanged(
                val state: State.LocationState,
            ) : Msg

            data class CameraPositionChanged(
                val position: CameraPosition,
            ) : Msg

            data class FirstLocationDetected(
                val point: Point,
            ) : Msg

            data class TimeUpdateChanged(
                val timeUpdate: Long,
            ) : Msg

            data class MarkersLoaded(
                val markers: List<GeoMarker>,
            ) : Msg

            data class MarkerSelected(
                val markerId: Long?,
            ) : Msg
        }

        private inner class BootstrapperImpl(
            private val markersFlow: Flow<List<GeoMarker>>,
            private val selectedMarkerIdFlow: StateFlow<Long?>,
        ) : CoroutineBootstrapper<Action>() {
            override fun invoke() {
                scope.launch {
                    determineCurrentLocation()
                }
                scope.launch {
                    updateMarkers(markersFlow)
                }
                scope.launch {
                    setSelectedMarker(selectedMarkerIdFlow)
                }
            }

            private suspend fun setSelectedMarker(selectedMarkerIdFlow: StateFlow<Long?>) {
                selectedMarkerIdFlow.collect { markerId ->
                    dispatch(Action.MarkerSelected(markerId))
                }
            }

            private suspend fun updateMarkers(markersFlow: Flow<List<GeoMarker>>) {
                markersFlow.collect { markers ->
                    dispatch(Action.MarkersUpdated(markers))
                }
            }

            private suspend fun determineCurrentLocation() {
                getCurrentLocation().collect { location ->
                    val state =
                        when (location) {
                            is LocationStatus.Available -> {
                                val timeUpdate = System.currentTimeMillis()
                                dispatch(Action.TimeUpdateChanged(timeUpdate))

                                State.LocationState.Available(location.point)
                            }

                            is LocationStatus.Loading ->
                                State.LocationState.Loading

                            is LocationStatus.Error ->
                                State.LocationState.Error

                            LocationStatus.Initial ->
                                State.LocationState.Initial
                        }
                    dispatch(Action.LocationStateChanged(state))
                }
            }
        }

        private inner class ExecutorImpl :
            CoroutineExecutor<Intent, Action, State, Msg, Label>() {
            private var lastCameraPosition: CameraPosition? = null

            override fun executeIntent(intent: Intent) {
                when (intent) {
                    Intent.StartLocationUpdates -> {
                        manageLocationUpdates.startLocationUpdates()
                    }

                    Intent.StopLocationUpdates -> {
                        manageLocationUpdates.stopLocationUpdates()
                    }

                    Intent.MoveToMyLocation -> {
                        val currentState = state()
                        if (currentState.locationState is State.LocationState.Available) {
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

                    is Intent.ChangeZoom -> {
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

                    is Intent.UpdateCameraPosition -> {
                        if (lastCameraPosition != intent.position) {
                            lastCameraPosition = intent.position
                            dispatch(Msg.CameraPositionChanged(intent.position))
                        }
                    }

                    is Intent.MarkerClicked -> {
                        dispatch(Msg.MarkerSelected(intent.markerId))
                        publish(Label.MarkerSelected(intent.markerId))
                    }
                }
            }

            override fun executeAction(action: Action) {
                when (action) {
                    is Action.CameraPositionChanged -> {
                    }

                    is Action.LocationStateChanged -> {
                        if (state().isFirstLocation &&
                            action.state is State.LocationState.Available
                        ) {
                            dispatch(Msg.FirstLocationDetected(action.state.point))
                        }

                        val currentTime = System.currentTimeMillis()
                        if (action.state is State.LocationState.Error &&
                            currentTime - state().timeUpdate < DELTA_CURRENT_AND_LAST_UPDATE_IN_MILLIS
                        ) {
                            return
                        }
                        dispatch(Msg.LocationStateChanged(action.state))
                    }

                    is Action.TimeUpdateChanged ->
                        dispatch(Msg.TimeUpdateChanged(action.timeUpdate))

                    is Action.MarkersUpdated -> {
                        dispatch(Msg.MarkersLoaded(action.markers))
                    }

                    is Action.MarkerSelected -> {
                        dispatch(Msg.MarkerSelected(action.markerId))
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

                    is Msg.FirstLocationDetected ->
                        copy(
                            isFirstLocation = false,
                            cameraPosition = CameraPosition(msg.point, DEFAULT_ZOOM_LEVEL, 0f, 0f),
                        )

                    is Msg.TimeUpdateChanged -> {
                        copy(timeUpdate = msg.timeUpdate)
                    }

                    is Msg.MarkersLoaded -> {
                        copy(markers = msg.markers)
                    }

                    is Msg.MarkerSelected -> {
                        copy(selectedMarkerId = msg.markerId)
                    }
                }
        }

        companion object {
            private const val DEFAULT_ZOOM_LEVEL = 15f
            private const val MIN_ZOOM = 1f
            private const val MAX_ZOOM = 20f
            private const val DELTA_CURRENT_AND_LAST_UPDATE_IN_MILLIS = 30_000L
        }
    }