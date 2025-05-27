package com.grebnev.feature.addmarker.presentation

import com.arkivanov.decompose.value.Value

interface AddMarkerComponent {
    val model: Value<AddMarkerStore.State>

    fun onIntent(intent: AddMarkerStore.Intent)
}