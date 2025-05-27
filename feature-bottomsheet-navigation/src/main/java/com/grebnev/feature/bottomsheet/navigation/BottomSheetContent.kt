package com.grebnev.feature.bottomsheet.navigation

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState

@Composable
fun BottomSheetContent(component: BottomSheetComponent) {
    val childStack = component.stack.subscribeAsState()

    Children(stack = component.stack) {
        when (val child = it.instance) {
            is BottomSheetComponent.Child.ListMarkers -> {
                ListMarkersContent(child.component)
            }
            is BottomSheetComponent.Child.DetailMarker -> {
                DetailMarkerContent(child.component)
            }
        }
    }
}