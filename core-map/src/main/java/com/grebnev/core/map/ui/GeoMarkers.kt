package com.grebnev.core.map.ui

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import com.grebnev.core.domain.entity.GeoMarker
import com.grebnev.core.map.R
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.map.TextStyle
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider

@Composable
internal fun GeoMarkers(
    context: Context,
    mapView: MapView,
    markers: List<GeoMarker>,
    selectedMarkerId: Long?,
    onMarkerClick: (GeoMarker) -> Unit,
    updateCameraPosition: (CameraPosition) -> Unit,
) {
    val map = mapView.mapWindow.map
    val markersCollection = remember { map.mapObjects.addCollection() }
    val currentMarkers = remember { mutableMapOf<Long, PlacemarkMapObject>() }
    val tapListeners = remember { mutableMapOf<Long, MapObjectTapListener>() }
    var previousSelectedId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(selectedMarkerId) {
        selectedMarkerId?.let { id ->
            markers.find { it.id == id }?.let { marker ->
                val targetPosition =
                    CameraPosition(
                        Point(marker.latitude, marker.longitude),
                        map.cameraPosition.zoom,
                        0f,
                        0f,
                    )
                updateCameraPosition(targetPosition)
            }
        }
    }

    LaunchedEffect(markers, selectedMarkerId) {
        deleteOutdatedMarkers(
            markers = markers,
            currentMarkers = currentMarkers,
            tapListeners = tapListeners,
            markersCollection = markersCollection,
        )

        if (selectedMarkerId != previousSelectedId) {
            resetPreviousMarkerSelection(
                context = context,
                markers = markers,
                previousSelectedId = previousSelectedId,
                currentMarkers = currentMarkers,
            )
            previousSelectedId = selectedMarkerId
        }

        markers.forEach { marker ->
            val isSelected = marker.id == selectedMarkerId
            val existingPlacemark = currentMarkers[marker.id]

            if (existingPlacemark != null) {
                updatePlacemark(
                    context = context,
                    placemark = existingPlacemark,
                    marker = marker,
                    isSelected = isSelected,
                )
            } else {
                val tapListener = createTapListener { onMarkerClick(marker) }
                val placemark =
                    createPlacemark(
                        context = context,
                        collection = markersCollection,
                        marker = marker,
                        isSelected = isSelected,
                        tapListener = tapListener,
                    )
                currentMarkers[marker.id] = placemark
                tapListeners[marker.id] = tapListener
            }
        }
    }
}

private fun deleteOutdatedMarkers(
    markers: List<GeoMarker>,
    currentMarkers: MutableMap<Long, PlacemarkMapObject>,
    tapListeners: MutableMap<Long, MapObjectTapListener>,
    markersCollection: MapObjectCollection,
) {
    val currentIds = markers.map { it.id }.toSet()
    val toRemove = currentMarkers.keys.filter { it !in currentIds }
    toRemove.forEach { id ->
        currentMarkers[id]?.let { placemark ->
            tapListeners[id]?.let { listener ->
                placemark.removeTapListener(listener)
            }
            markersCollection.remove(placemark)
        }
        currentMarkers.remove(id)
        tapListeners.remove(id)
    }
}

private fun resetPreviousMarkerSelection(
    context: Context,
    markers: List<GeoMarker>,
    previousSelectedId: Long?,
    currentMarkers: MutableMap<Long, PlacemarkMapObject>,
) {
    previousSelectedId?.let { oldId ->
        currentMarkers[oldId]?.let { oldPlacemark ->
            updatePlacemark(
                context = context,
                placemark = oldPlacemark,
                marker = markers.find { it.id == oldId } ?: return@let,
                isSelected = false,
            )
        }
    }
}

private fun updatePlacemark(
    context: Context,
    placemark: PlacemarkMapObject,
    marker: GeoMarker,
    isSelected: Boolean,
) {
    val iconScale = if (isSelected) 1.5f else 1f
    val icon = createScaledIcon(context, R.drawable.ic_marker, iconScale)

    placemark.apply {
        geometry = Point(marker.latitude, marker.longitude)
        setIcon(icon)
        setText(
            marker.title,
            createTextStyle(isSelected),
        )
    }
}

private fun createPlacemark(
    context: Context,
    collection: MapObjectCollection,
    marker: GeoMarker,
    isSelected: Boolean,
    tapListener: MapObjectTapListener,
): PlacemarkMapObject {
    val iconScale = if (isSelected) 1.5f else 1f
    val icon = createScaledIcon(context, R.drawable.ic_marker, iconScale)

    return collection.addPlacemark().apply {
        geometry = Point(marker.latitude, marker.longitude)
        setIcon(icon)
        setText(
            marker.title,
            createTextStyle(isSelected),
        )
        addTapListener(tapListener)
    }
}

private fun createTapListener(onClick: () -> Unit): MapObjectTapListener =
    MapObjectTapListener { _, _ ->
        onClick()
        true
    }

private fun createTextStyle(isSelected: Boolean): TextStyle =
    TextStyle().apply {
        size = if (isSelected) 12f else 10f
        placement = TextStyle.Placement.BOTTOM
        offset = 5f
        color = Color.Blue.toArgb()
    }

private fun createScaledIcon(
    context: Context,
    @DrawableRes iconRes: Int,
    scale: Float,
): ImageProvider {
    val originalBitmap = ContextCompat.getDrawable(context, iconRes)?.toBitmap() ?: createBitmap(1, 1)

    val scaledWidth = (originalBitmap.width * scale).toInt()
    val scaledHeight = (originalBitmap.height * scale).toInt()

    val scaledBitmap = originalBitmap.scale(scaledWidth, scaledHeight)

    return ImageProvider.fromBitmap(scaledBitmap)
}