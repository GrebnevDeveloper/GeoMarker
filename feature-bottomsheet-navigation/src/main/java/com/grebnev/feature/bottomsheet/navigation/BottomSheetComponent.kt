package com.grebnev.feature.bottomsheet.navigation

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.grebnev.feature.listmarkers.ListMarkersComponent

interface BottomSheetComponent {
    val stack: Value<ChildStack<*, Child>>

    fun navigateToDetail(markerId: Long)

    fun onBackPressed(): Boolean

    sealed interface Child {
        data class ListMarkers(
            val component: ListMarkersComponent,
        ) : Child
    }
}