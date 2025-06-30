package com.grebnev.feature.geomarker.api

import com.arkivanov.mvikotlin.core.store.Store
import com.grebnev.core.common.wrappers.Result
import com.grebnev.core.domain.entity.GeoMarker
import com.grebnev.feature.geomarker.api.GeoMarkerStore.Intent
import com.grebnev.feature.geomarker.api.GeoMarkerStore.Label
import com.grebnev.feature.geomarker.api.GeoMarkerStore.State

interface GeoMarkerStore : Store<Intent, State, Label> {
    sealed interface Intent {
        data class SelectMarker(
            val marker: GeoMarker?,
        ) : Intent

        data object AddMarkerClicked : Intent
    }

    data class State(
        val markersResult: Result<List<GeoMarker>>,
        val selectedMarker: GeoMarker?,
    )

    sealed interface Label {
        data object AddMarkerClicked : Label
    }
}