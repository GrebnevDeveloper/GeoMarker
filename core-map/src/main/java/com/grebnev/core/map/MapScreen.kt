package com.grebnev.core.map

import android.Manifest
import android.content.Context
import android.view.ViewGroup
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.grebnev.core.location.domain.LocationState
import com.grebnev.core.permissions.PermissionRequired
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    mapViewModel: MapViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val mapView = rememberMapViewWithLifecycle(context)
    val locationState = mapViewModel.locationState.collectAsState()

    PermissionRequired(
        context = context,
        permission = Manifest.permission.ACCESS_FINE_LOCATION,
        permissionDescription = stringResource(R.string.location_access),
    ) { permissionState ->
        LaunchedEffect(permissionState.status) {
            if (permissionState.status.isGranted) {
                mapViewModel.startUpdates()
            } else {
                mapViewModel.stopUpdates()
            }
        }

        Box(modifier = modifier.fillMaxSize()) {
            AndroidView(
                modifier = modifier.fillMaxSize(),
                factory = {
                    mapView.apply {
                        layoutParams =
                            ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT,
                            )
                    }
                },
            )
            if (permissionState.status.isGranted) {
                CurrentLocationMarker(context, mapView, locationState.value)
            }

            Column(
                modifier =
                    modifier
                        .align(Alignment.CenterEnd)
                        .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ZoomButton(Icons.Rounded.Add) { changeZoom(mapView, 1f) }
                ZoomButton(Icons.Rounded.Remove) { changeZoom(mapView, -1f) }
                LocationButton(
                    state = locationState.value,
                    hasPermission = permissionState.status.isGranted,
                    onMoveToMyLocation = { moveToMyLocation(mapView, locationState.value) },
                    onRequestPermission = { permissionState.launchPermissionRequest() },
                )
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mapViewModel.stopUpdates()
        }
    }
}

@Composable
private fun CurrentLocationMarker(
    context: Context,
    mapView: MapView,
    state: LocationState,
) {
    val map = mapView.mapWindow.map

    var isFirstLocation by remember { mutableStateOf(true) }

    LaunchedEffect(state) {
        if (state is LocationState.Available) {
            map.mapObjects.clear()
            map.mapObjects.addPlacemark().apply {
                geometry = state.point
                setIcon(ImageProvider.fromResource(context, R.drawable.ic_my_location))
            }

            if (isFirstLocation) {
                map.move(CameraPosition(state.point, 15f, 0f, 0f))
                isFirstLocation = false
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
    state: LocationState,
    hasPermission: Boolean,
    onMoveToMyLocation: () -> Unit,
    onRequestPermission: () -> Unit,
) {
    val icon =
        when {
            !hasPermission -> Icons.Rounded.LocationDisabled
            state is LocationState.Loading -> Icons.Rounded.Refresh
            state is LocationState.Error -> Icons.Rounded.LocationOff
            else -> Icons.Rounded.MyLocation
        }
    FloatingActionButton(
        modifier = Modifier.size(48.dp),
        onClick = {
            if (!hasPermission) {
                onRequestPermission()
            } else if (state is LocationState.Available) {
                onMoveToMyLocation()
            }
        },
        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        if (state is LocationState.Loading) {
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

private fun changeZoom(
    mapView: MapView,
    delta: Float,
) {
    mapView.mapWindow.map.cameraPosition.let { current ->
        val newZoom = (current.zoom + delta).coerceIn(1f..20f)
        mapView.mapWindow.map.move(
            CameraPosition(current.target, newZoom, current.azimuth, current.tilt),
        )
    }
}

private fun moveToMyLocation(
    mapView: MapView,
    state: LocationState,
) {
    if (state is LocationState.Available) {
        mapView.mapWindow.map.move(
            CameraPosition(state.point, 15f, 0f, 0f),
        )
    }
}