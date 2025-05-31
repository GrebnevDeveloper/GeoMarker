@file:OptIn(ExperimentalCoroutinesApi::class)

package com.grebnev.feature.detailsmarker.presentation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.grebnev.core.extensions.componentScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

class DefaultDetailsMarkerComponent
    @AssistedInject
    constructor(
        private val detailsStoreFactory: DetailsMarkerStoreFactory,
        @Assisted private val markerId: Long,
        @Assisted private val onBackClicked: () -> Unit,
        @Assisted component: ComponentContext,
    ) : DetailsMarkerComponent,
        ComponentContext by component {
        private val store = instanceKeeper.getStore { detailsStoreFactory.create(markerId) }

        private val _model = MutableValue(store.state)
        override val model: Value<DetailsMarkerStore.State> = _model

        init {
            componentScope().launch {
                store.stateFlow.collect { newState ->
                    _model.value = newState
                }
            }
            componentScope().launch {
                store.labels.collect { label ->
                    when (label) {
                        DetailsMarkerStore.Label.BackClicked -> {
                            onBackClicked()
                        }
                    }
                }
            }
        }

        override fun onIntent(intent: DetailsMarkerStore.Intent) {
            store.accept(intent)
        }

        @AssistedFactory
        interface Factory {
            fun create(
                @Assisted markerId: Long,
                @Assisted onBackClicked: () -> Unit,
                @Assisted component: ComponentContext,
            ): DefaultDetailsMarkerComponent
        }
    }