package com.grebnev.core.map.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.LocationDisabled
import androidx.compose.material.icons.rounded.LocationOff
import androidx.compose.material.icons.rounded.LocationSearching
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.grebnev.core.map.presentation.MapStore

@Composable
internal fun MapControls(
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
            !hasPermission -> Icons.Rounded.LocationOff
            state is MapStore.State.LocationState.Loading -> Icons.Rounded.LocationSearching
            state is MapStore.State.LocationState.Error -> Icons.Rounded.LocationDisabled
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