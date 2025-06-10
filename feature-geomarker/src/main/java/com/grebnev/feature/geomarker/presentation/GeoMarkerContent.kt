@file:OptIn(ExperimentalPermissionsApi::class)

package com.grebnev.feature.geomarker.presentation

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddLocationAlt
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.grebnev.core.map.presentation.MapContent
import com.grebnev.core.permissions.PermissionConstants
import com.grebnev.core.permissions.multiple.MultiplePermissionsRequest
import com.grebnev.feature.bottomsheet.navigation.BottomSheetComponent
import com.grebnev.feature.bottomsheet.navigation.BottomSheetContent
import com.grebnev.feature.geomarker.api.GeoMarkerStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeoMarkerContent(component: GeoMarkerComponent) {
    val sheetState =
        rememberBottomSheetScaffoldState(
            bottomSheetState =
                rememberStandardBottomSheetState(
                    initialValue = SheetValue.PartiallyExpanded,
                    skipHiddenState = false,
                ),
        )
    val store by component.model.subscribeAsState()

    LaunchedEffect(store.selectedMarker) {
        if (store.selectedMarker == null) {
            sheetState.bottomSheetState.partialExpand()
        } else {
            sheetState.bottomSheetState.expand()
        }
    }

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    MultiplePermissionsRequest(
        permissions = PermissionConstants.requiredPermissions(),
    ) { permissionsState ->
        BottomSheetScaffold(
            scaffoldState = sheetState,
            sheetPeekHeight = screenHeight / 4,
            content = { paddingValues ->
                MapMarkerSection(
                    permissionsState = permissionsState,
                    component = component,
                    screenHeight = screenHeight,
                )
            },
            sheetContent = {
                BottomSheetSection(
                    component = component.bottomSheetComponent,
                    permissionsState = permissionsState,
                    screenHeight = screenHeight,
                )
            },
        )
    }
}

@Composable
private fun MapMarkerSection(
    permissionsState: List<PermissionState>,
    component: GeoMarkerComponent,
    screenHeight: Dp,
) {
    val locationPermissionState =
        permissionsState.find { it.permission == Manifest.permission.ACCESS_FINE_LOCATION }
            ?: rememberPermissionState(
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
    Box {
        MapContent(
            component = component.mapComponent,
            locationPermissionState = locationPermissionState,
            showCurrentLocation = true,
            showMarkers = true,
        )
        FloatingActionButton(
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 10.dp, bottom = screenHeight / 4 + 5.dp)
                    .size(70.dp),
            onClick = { component.onIntent(GeoMarkerStore.Intent.AddMarkerClicked) },
            shape = CircleShape,
        ) {
            Icon(
                modifier = Modifier.size(35.dp),
                imageVector = Icons.Rounded.AddLocationAlt,
                contentDescription = null,
            )
        }
    }
}

@Composable
fun BottomSheetSection(
    component: BottomSheetComponent,
    permissionsState: List<PermissionState>,
    screenHeight: Dp,
) {
    val storagePermissionName =
        if (Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    val storagePermissionState =
        permissionsState.find { it.permission == storagePermissionName }
            ?: rememberPermissionState(storagePermissionName)
    Box(
        modifier =
            Modifier
                .height(screenHeight / 5 * 4)
                .fillMaxWidth(),
    ) {
        BottomSheetContent(
            component = component,
            hasStoragePermission = storagePermissionState.status.isGranted,
        )
    }
}