package com.grebnev.feature.bottomsheet.navigation

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.grebnev.feature.detailsmarker.presentation.DetailsMarkerComponent
import com.grebnev.feature.listmarkers.ListMarkersComponent

interface BottomSheetComponent {
    val stack: Value<ChildStack<*, Child>>

    sealed interface Child {
        data class ListMarkers(
            val component: ListMarkersComponent,
        ) : Child

        data class DetailsMarker(
            val component: DetailsMarkerComponent,
        ) : Child
    }
}