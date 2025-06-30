package com.grebnev.core.map.presentation

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.grebnev.core.common.wrappers.Result
import com.grebnev.core.domain.entity.GeoMarker
import com.grebnev.core.location.domain.entity.LocationStatus
import com.grebnev.core.location.domain.usecase.GetCurrentLocationUseCase
import com.grebnev.core.location.domain.usecase.ManageLocationUpdatesUseCase
import com.grebnev.core.map.domain.GetLastPositionUseCase
import com.grebnev.core.map.domain.UpdateLastPositionUseCase
import com.grebnev.core.map.extensions.defaultCameraPosition
import com.grebnev.core.map.presentation.MapStore.Intent
import com.grebnev.core.map.presentation.MapStore.Label
import com.grebnev.core.map.presentation.MapStore.State
import com.grebnev.feature.geomarker.api.GeoMarkerStore
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
            val marker: GeoMarker,
        ) : Intent
    }

    data class State(
        val locationState: LocationState,
        val cameraPosition: CameraPosition?,
        val isFirstLocation: Boolean,
        val timeUpdate: Long,
        val markersResult: Result<List<GeoMarker>>,
        val selectedMarker: GeoMarker?,
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
            val marker: GeoMarker?,
        ) : Label

        data class CameraPositionChanged(
            val position: CameraPosition,
        ) : Label
    }
}

class MapStoreFactory
    @Inject
    constructor(
        private val storeFactory: StoreFactory,
        private val manageLocationUpdates: ManageLocationUpdatesUseCase,
        private val getCurrentLocation: GetCurrentLocationUseCase,
        private val updateLastPositionUseCase: UpdateLastPositionUseCase,
        private val getLastPositionUseCase: GetLastPositionUseCase,
    ) {
        fun create(geoMarkerStore: GeoMarkerStore?): MapStore =
            object :
                MapStore,
                Store<Intent, State, Label> by storeFactory.create(
                    name = "MapStore",
                    initialState =
                        State(
                            locationState = State.LocationState.Initial,
                            cameraPosition = null,
                            isFirstLocation = geoMarkerStore != null,
                            timeUpdate = 0L,
                            markersResult = Result.empty(),
                            selectedMarker = null,
                        ),
                    bootstrapper = BootstrapperImpl(geoMarkerStore),
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

            data class UpdateLastPositionChanged(
                val position: CameraPosition,
            ) : Action

            data class TimeUpdateChanged(
                val timeUpdate: Long,
            ) : Action

            data class MarkersUpdated(
                val markersResult: Result<List<GeoMarker>>,
            ) : Action

            data class MarkerSelected(
                val marker: GeoMarker?,
            ) : Action
        }

        private sealed interface Msg {
            data class LocationStateChanged(
                val state: State.LocationState,
            ) : Msg

            data class CameraPositionChanged(
                val position: CameraPosition,
            ) : Msg

            data object FirstLocationDetected : Msg

            data class TimeUpdateChanged(
                val timeUpdate: Long,
            ) : Msg

            data class MarkersLoaded(
                val markersResult: Result<List<GeoMarker>>,
            ) : Msg

            data class MarkerSelected(
                val marker: GeoMarker?,
            ) : Msg
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private inner class BootstrapperImpl(
            private val geoMarkerStore: GeoMarkerStore?,
        ) : CoroutineBootstrapper<Action>() {
            override fun invoke() {
                scope.launch {
                    val position = getLastPositionUseCase()
                    if (position != null) {
                        dispatch(Action.UpdateLastPositionChanged(position.defaultCameraPosition))
                    }
                }
                scope.launch {
                    determineCurrentLocation()
                }
                geoMarkerStore?.let { store ->
                    scope.launch {
                        updateMarkers(store)
                    }
                    scope.launch {
                        setSelectedMarker(store)
                    }
                }
            }

            private suspend fun setSelectedMarker(store: GeoMarkerStore) {
                store.stateFlow.collect { state ->
                    dispatch(Action.MarkerSelected(state.selectedMarker))
                }
            }

            private suspend fun updateMarkers(store: GeoMarkerStore) {
                store.stateFlow.collect { state ->
                    dispatch(Action.MarkersUpdated(state.markersResult))
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
                            val position = currentState.locationState.point.defaultCameraPosition
                            moveCameraPosition(position)
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
                        moveCameraPosition(intent.position)
                    }

                    is Intent.MarkerClicked -> {
                        dispatch(Msg.MarkerSelected(intent.marker))
                        publish(Label.MarkerSelected(intent.marker))
                    }
                }
            }

            override fun executeAction(action: Action) {
                when (action) {
                    is Action.CameraPositionChanged -> {
                        publish(Label.CameraPositionChanged(action.position))
                    }

                    is Action.LocationStateChanged -> {
                        if (state().isFirstLocation &&
                            action.state is State.LocationState.Available
                        ) {
                            dispatch(Msg.FirstLocationDetected)
                            moveCameraPosition(action.state.point.defaultCameraPosition)
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
                        dispatch(Msg.MarkersLoaded(action.markersResult))
                    }

                    is Action.MarkerSelected -> {
                        dispatch(Msg.MarkerSelected(action.marker))
                    }

                    is Action.UpdateLastPositionChanged -> {
                        dispatch(Msg.CameraPositionChanged(action.position))
                    }
                }
            }

            private fun moveCameraPosition(position: CameraPosition) {
                if (lastCameraPosition != position) {
                    lastCameraPosition = position
                    dispatch(Msg.CameraPositionChanged(position))
                    publish(Label.CameraPositionChanged(position))
                    scope.launch {
                        updateLastPositionUseCase(position.target)
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

                    Msg.FirstLocationDetected ->
                        copy(isFirstLocation = false)

                    is Msg.TimeUpdateChanged -> {
                        copy(timeUpdate = msg.timeUpdate)
                    }

                    is Msg.MarkersLoaded -> {
                        copy(markersResult = msg.markersResult)
                    }

                    is Msg.MarkerSelected -> {
                        copy(selectedMarker = msg.marker)
                    }
                }
        }

        companion object {
            private const val DELTA_CURRENT_AND_LAST_UPDATE_IN_MILLIS = 30_000L
            const val MIN_ZOOM = 1f
            const val MAX_ZOOM = 20f
        }
    }