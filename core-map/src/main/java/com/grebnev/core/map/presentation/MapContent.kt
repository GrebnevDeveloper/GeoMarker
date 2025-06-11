package com.grebnev.core.map.presentation

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.grebnev.core.map.extensions.hasSignificantDifferenceFrom
import com.grebnev.core.map.ui.CurrentLocationMarker
import com.grebnev.core.map.ui.GeoMarkers
import com.grebnev.core.map.ui.MapControls
import com.grebnev.core.map.ui.PositionMarker
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CameraUpdateReason
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.mapview.MapView

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapContent(
    component: MapComponent,
    locationPermissionState: PermissionState,
    modifier: Modifier = Modifier,
    showCurrentLocation: Boolean = false,
    showMarkers: Boolean = false,
    showPositionMarker: Boolean = false,
) {
    val state by component.model.subscribeAsState()
    val context = LocalContext.current
    val mapView = rememberMapViewWithLifecycle(context)

    HandleFirstLocation(
        state = state,
        mapView = mapView,
    )

    SynchronizationPositionWithStore(
        mapView = mapView,
        cameraPosition = state.cameraPosition,
        updateCameraPosition = { position ->
            component.onIntent(MapStore.Intent.UpdateCameraPosition(position))
        },
    )

    LaunchedEffect(locationPermissionState.status) {
        if (locationPermissionState.status.isGranted) {
            component.onIntent(MapStore.Intent.StartLocationUpdates)
        } else {
            component.onIntent(MapStore.Intent.StopLocationUpdates)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = modifier.fillMaxSize(),
            factory = { mapView },
        )
        if (locationPermissionState.status.isGranted) {
            if (showCurrentLocation) {
                CurrentLocationMarker(
                    context = context,
                    mapView = mapView,
                    locationState = state.locationState,
                )
            }
        }

        MapControls(
            modifier =
                Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp, bottom = 20.dp),
            locationState = state.locationState,
            hasPermission = locationPermissionState.status.isGranted,
            onRequestPermission = { locationPermissionState.launchPermissionRequest() },
            onMoveToMyLocation = { component.onIntent(MapStore.Intent.MoveToMyLocation) },
            onChangeZoom = { delta -> component.onIntent(MapStore.Intent.ChangeZoom(delta)) },
        )

        if (showMarkers) {
            GeoMarkers(
                context = context,
                mapView = mapView,
                markers = state.markers,
                selectedMarkerId = state.selectedMarker?.id,
                onMarkerClick = { marker ->
                    component.onIntent(MapStore.Intent.MarkerClicked(marker))
                },
                updateCameraPosition = { position ->
                    component.onIntent(MapStore.Intent.UpdateCameraPosition(position))
                },
            )
        }
        if (showPositionMarker) {
            PositionMarker(modifier = Modifier.align(Alignment.Center))
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            component.onIntent(MapStore.Intent.StopLocationUpdates)
        }
    }
}

@Composable
private fun HandleFirstLocation(
    state: MapStore.State,
    mapView: MapView,
) {
    LaunchedEffect(state.isFirstLocation) {
        if (!state.isFirstLocation && state.cameraPosition != null) {
            state.cameraPosition.let { position ->
                mapView.mapWindow.map.move(position)
            }
        }
    }
}

@Composable
private fun SynchronizationPositionWithStore(
    mapView: MapView,
    cameraPosition: CameraPosition?,
    updateCameraPosition: (CameraPosition) -> Unit,
) {
    LaunchedEffect(cameraPosition) {
        cameraPosition?.let { targetPosition ->
            if (mapView.mapWindow.map.cameraPosition
                    .hasSignificantDifferenceFrom(targetPosition)
            ) {
                mapView.mapWindow.map.move(
                    targetPosition,
                    Animation(Animation.Type.SMOOTH, 0.3f),
                    null,
                )
            }
        }
    }

    DisposableEffect(mapView) {
        val map = mapView.mapWindow.map
        val listener =
            object : CameraListener {
                override fun onCameraPositionChanged(
                    map: Map,
                    position: CameraPosition,
                    cameraUpdateReason: CameraUpdateReason,
                    finished: Boolean,
                ) {
                    if (cameraUpdateReason == CameraUpdateReason.GESTURES && finished) {
                        updateCameraPosition(position)
                    }
                }
            }

        map.addCameraListener(listener)

        onDispose {
            map.removeCameraListener(listener)
        }
    }
}

@Composable
private fun rememberMapViewWithLifecycle(context: Context): MapView {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val mapView = remember { MapView(context) }

    DisposableEffect(lifecycle, mapView) {
        val observer =
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_START -> {
                        MapKitFactory.getInstance().onStart()
                        mapView.onStart()
                    }

                    Lifecycle.Event.ON_STOP -> {
                        mapView.onStop()
                        MapKitFactory.getInstance().onStop()
                    }

                    else -> {}
                }
            }

        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    return mapView
}