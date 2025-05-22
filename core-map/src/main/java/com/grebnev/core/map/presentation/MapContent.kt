package com.grebnev.core.map.presentation

import android.Manifest
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.LocationDisabled
import androidx.compose.material.icons.rounded.LocationOff
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.grebnev.core.map.R
import com.grebnev.core.map.extensions.hasSignificantDifferenceFrom
import com.grebnev.core.permissions.PermissionRequired
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CameraUpdateReason
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapContent(
    component: MapComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.model.subscribeAsState()
    val context = LocalContext.current
    val mapView = rememberMapViewWithLifecycle(context)

    LaunchedEffect(state.isFirstLocation) {
        if (!state.isFirstLocation && state.cameraPosition != null) {
            state.cameraPosition?.let { position ->
                mapView.mapWindow.map.move(position)
            }
        }
    }

    SynchronizationPositionWithStore(
        mapView = mapView,
        cameraPosition = state.cameraPosition,
        updateCameraPosition = { position ->
            component.onIntent(MapStore.Intent.UpdateCameraPosition(position))
        },
    )

    PermissionRequired(
        context = context,
        permission = Manifest.permission.ACCESS_FINE_LOCATION,
        permissionDescription = stringResource(R.string.location_access),
    ) { permissionState ->
        LaunchedEffect(permissionState.status) {
            if (permissionState.status.isGranted) {
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
            if (permissionState.status.isGranted) {
                CurrentLocationMarker(
                    context = context,
                    mapView = mapView,
                    locationState = state.locationState,
                )
            }

            MapControls(
                modifier =
                    Modifier
                        .align(Alignment.CenterEnd)
                        .padding(16.dp),
                locationState = state.locationState,
                hasPermission = permissionState.status.isGranted,
                onRequestPermission = { permissionState.launchPermissionRequest() },
                onMoveToMyLocation = { component.onIntent(MapStore.Intent.MoveToMyLocation) },
                onChangeZoom = { delta -> component.onIntent(MapStore.Intent.ChangeZoom(delta)) },
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            component.onIntent(MapStore.Intent.StopLocationUpdates)
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
private fun MapControls(
    modifier: Modifier = Modifier,
    locationState: MapStore.State.LocationState,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onMoveToMyLocation: () -> Unit,
    onChangeZoom: (Float) -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ZoomButton(Icons.Rounded.Add) { onChangeZoom(1f) }
        ZoomButton(Icons.Rounded.Remove) { onChangeZoom(-1f) }
        LocationButton(
            state = locationState,
            hasPermission = hasPermission,
            onMoveToMyLocation = onMoveToMyLocation,
            onRequestPermission = onRequestPermission,
        )
    }
}

@Composable
private fun CurrentLocationMarker(
    context: Context,
    mapView: MapView,
    locationState: MapStore.State.LocationState,
) {
    val map = mapView.mapWindow.map

    LaunchedEffect(locationState) {
        when (locationState) {
            is MapStore.State.LocationState.Available -> {
                map.mapObjects.clear()
                map.mapObjects.addPlacemark().apply {
                    geometry = locationState.point
                    setIcon(ImageProvider.fromResource(context, R.drawable.ic_my_location))
                }
            }

            else -> {
            }
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

@Composable
private fun ZoomButton(
    icon: ImageVector,
    onClickListener: () -> Unit,
) {
    FloatingActionButton(
        modifier = Modifier.size(48.dp),
        onClick = onClickListener,
        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = icon,
            contentDescription = null,
        )
    }
}

@Composable
private fun LocationButton(
    state: MapStore.State.LocationState,
    hasPermission: Boolean,
    onMoveToMyLocation: () -> Unit,
    onRequestPermission: () -> Unit,
) {
    val icon =
        when {
            !hasPermission -> Icons.Rounded.LocationDisabled
            state is MapStore.State.LocationState.Loading -> Icons.Rounded.Refresh
            state is MapStore.State.LocationState.Error -> Icons.Rounded.LocationOff
            else -> Icons.Rounded.MyLocation
        }
    FloatingActionButton(
        modifier = Modifier.size(48.dp),
        onClick = {
            if (!hasPermission) {
                onRequestPermission()
            } else if (state is MapStore.State.LocationState.Available) {
                onMoveToMyLocation()
            }
        },
        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        if (state is MapStore.State.LocationState.Loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        } else {
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = icon,
                contentDescription = null,
            )
        }
    }
}