package com.grebnev.feature.detailsmarker

import com.arkivanov.decompose.value.Value

interface DetailsMarkerComponent {
    val model: Value<DetailsMarkerStore.State>

    fun onIntent(intent: DetailsMarkerStore.Intent)
}