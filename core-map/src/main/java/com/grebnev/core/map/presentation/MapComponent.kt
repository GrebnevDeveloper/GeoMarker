package com.grebnev.core.map.presentation

import com.arkivanov.decompose.value.Value

interface MapComponent {
    val model: Value<MapStore.State>

    fun onIntent(intent: MapStore.Intent)
}