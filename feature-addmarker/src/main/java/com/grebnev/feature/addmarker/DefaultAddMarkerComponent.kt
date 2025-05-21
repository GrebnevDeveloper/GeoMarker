package com.grebnev.feature.addmarker

import com.arkivanov.decompose.ComponentContext
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class DefaultAddMarkerComponent
    @AssistedInject
    constructor(
        @Assisted private val onBackClicked: () -> Unit,
        @Assisted componentContext: ComponentContext,
    ) : AddMarkerComponent,
        ComponentContext by componentContext {
        override fun onBackClicked() = onBackClicked.invoke()

        @AssistedFactory
        interface Factory {
            fun create(
                @Assisted onBackClicked: () -> Unit,
                @Assisted componentContext: ComponentContext,
            ): DefaultAddMarkerComponent
        }
    }