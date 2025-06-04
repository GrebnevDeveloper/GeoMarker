package com.grebnev.core.map.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.grebnev.core.map.R
import com.grebnev.core.map.presentation.MapStore
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider

@Composable
internal fun CurrentLocationMarker(
    context: Context,
    mapView: MapView,
    locationState: MapStore.State.LocationState,
) {
    val map = mapView.mapWindow.map
    val locationCollection = remember { map.mapObjects.addCollection() }

    LaunchedEffect(locationState) {
        when (locationState) {
            is MapStore.State.LocationState.Available -> {
                locationCollection.clear()
                locationCollection.addPlacemark().apply {
                    geometry = locationState.point
                    setIcon(ImageProvider.fromResource(context, R.drawable.ic_my_location))
                }
            }

            else -> {
            }
        }
    }
}