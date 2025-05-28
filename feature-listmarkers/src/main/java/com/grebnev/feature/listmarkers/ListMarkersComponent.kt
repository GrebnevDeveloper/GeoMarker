package com.grebnev.feature.listmarkers

import com.arkivanov.decompose.value.Value

interface ListMarkersComponent {
    val model: Value<ListMarkersStore.State>

    fun onIntent(intent: ListMarkersStore.Intent)
}