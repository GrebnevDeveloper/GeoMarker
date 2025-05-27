package com.grebnev.feature.bottomsheet.navigation

import com.arkivanov.decompose.Child
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value

interface BottomSheetComponent {
    val stack: Value<ChildStack<*, Child>>

    fun navigateToList()

    fun navigateToDetail(markerId: String)

    fun onBackPressed(): Boolean

    sealed interface Child {
        data class ListMarkers(
            val component: ListMarkersComponent,
        ) : Child

        data class DetailMarker(
            val component: DetailMarkerComponent,
        ) : Child
    }
}