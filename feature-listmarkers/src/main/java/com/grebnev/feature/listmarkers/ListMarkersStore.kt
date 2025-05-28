package com.grebnev.feature.listmarkers

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.grebnev.core.domain.entity.GeoMarker
import com.grebnev.feature.listmarkers.ListMarkersStore.Intent
import com.grebnev.feature.listmarkers.ListMarkersStore.Label
import com.grebnev.feature.listmarkers.ListMarkersStore.State
import javax.inject.Inject

interface ListMarkersStore : Store<Intent, State, Label> {
    sealed interface Intent {
        data class MarkerClicked(
            val markerId: Long,
        ) : Intent
    }

    data class State(
        val markers: List<GeoMarker> = emptyList(),
    )

    sealed interface Label {
        data class MarkerClicked(
            val markerId: Long,
        ) : Label
    }
}

class ListMarkersStoreFactory
    @Inject
    constructor(
        private val storeFactory: StoreFactory,
    ) {
        fun create(markers: List<GeoMarker>): ListMarkersStore =
            object :
                ListMarkersStore,
                Store<Intent, State, Label> by storeFactory.create(
                    name = "ListMarkersStore",
                    initialState =
                        State(
                            markers = markers,
                        ),
                    bootstrapper = null,
                    executorFactory = ::ExecutorImpl,
                    reducer = ReducerImpl,
                ) {}

        private sealed interface Action {
            data class MarkersUpdated(
                val markers: List<GeoMarker>,
            ) : Action
        }

        private sealed interface Msg {
            data class MarkersLoaded(
                val markers: List<GeoMarker>,
            ) : Msg
        }

        private inner class ExecutorImpl : CoroutineExecutor<Intent, Action, State, Msg, Label>() {
            override fun executeIntent(intent: Intent) {
                when (intent) {
                    is Intent.MarkerClicked -> {
                        publish(Label.MarkerClicked(intent.markerId))
                    }
                }
            }

            override fun executeAction(action: Action) {
                when (action) {
                    is Action.MarkersUpdated -> {
                        dispatch(Msg.MarkersLoaded(action.markers))
                    }
                }
            }
        }

        private object ReducerImpl : Reducer<State, Msg> {
            override fun State.reduce(msg: Msg): State =
                when (msg) {
                    is Msg.MarkersLoaded ->
                        copy(
                            markers = msg.markers,
                        )
                }
        }
    }