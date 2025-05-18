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
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.grebnev.core.permissions.PermissionRequired
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider

@Composable
fun MapScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val mapView = rememberMapViewWithLifecycle(context)

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
        Column(
            modifier =
                modifier
                    .align(Alignment.CenterEnd)
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ZoomButton(Icons.Rounded.Add) { changeZoom(mapView, 1f) }
            ZoomButton(Icons.Rounded.Remove) { changeZoom(mapView, -1f) }
            LocationButton { moveToLocation(mapView) }
        }
    }

    CurrentLocationMarker(context, mapView)
}

@Composable
private fun CurrentLocationMarker(
    context: Context,
    mapView: MapView,
) {
    val map = mapView.mapWindow.map

    PermissionRequired(
        context = context,
        permission = Manifest.permission.ACCESS_FINE_LOCATION,
        permissionDescription = stringResource(R.string.location_access),
    ) { isGranted ->
        if (isGranted) {
            map.move(
                CameraPosition(Point(55.751574, 37.573856), 11f, 0f, 0f),
            )

            map.mapObjects.addPlacemark().apply {
                geometry = Point(55.751574, 37.573856)
                setIcon(ImageProvider.fromResource(context, R.drawable.ic_my_location))
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
private fun LocationButton(onClickListener: () -> Unit) {
    FloatingActionButton(
        modifier = Modifier.size(48.dp),
        onClick = onClickListener,
        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = Icons.Rounded.MyLocation,
            contentDescription = null,
        )
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

private fun moveToLocation(mapView: MapView) {
}