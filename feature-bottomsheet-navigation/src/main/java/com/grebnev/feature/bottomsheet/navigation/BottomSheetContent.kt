package com.grebnev.feature.bottomsheet.navigation

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.grebnev.feature.detailsmarker.presentation.DetailsMarkerScreen
import com.grebnev.feature.listmarkers.ListMarkersContent

@Composable
fun BottomSheetContent(
    component: BottomSheetComponent,
    hasStoragePermission: Boolean,
) {
    Children(stack = component.stack) {
        when (val child = it.instance) {
            is BottomSheetComponent.Child.ListMarkers -> {
                ListMarkersContent(child.component)
            }

            is BottomSheetComponent.Child.DetailsMarker -> {
                DetailsMarkerScreen(
                    component = child.component,
                    hasStoragePermission = hasStoragePermission,
                )
            }
        }
    }
}