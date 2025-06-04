package com.grebnev.feature.geomarker.presentation

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.grebnev.core.map.presentation.MapContent
import com.grebnev.feature.bottomsheet.navigation.BottomSheetContent

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
    val selectedMarkerId by store.selectedMarkerId.collectAsState()

    LaunchedEffect(selectedMarkerId) {
        if (selectedMarkerId == null) {
            sheetState.bottomSheetState.partialExpand()
        } else {
            sheetState.bottomSheetState.expand()
        }
    }

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    BottomSheetScaffold(
        scaffoldState = sheetState,
        sheetPeekHeight = screenHeight / 4,
        content = { paddingValues ->
            Box {
                MapContent(
                    component = component.mapComponent,
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
        },
        sheetContent = {
            Box(
                modifier =
                    Modifier
                        .height(screenHeight / 5 * 4)
                        .fillMaxWidth(),
            ) {
                BottomSheetContent(component.bottomSheetComponent)
            }
        },
    )
}