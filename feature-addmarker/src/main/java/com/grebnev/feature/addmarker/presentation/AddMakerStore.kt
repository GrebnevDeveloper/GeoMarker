package com.grebnev.feature.addmarker.presentation

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.grebnev.core.domain.entity.GeoMarker
import com.grebnev.feature.addmarker.domain.AddGeoMarkerUseCase
import com.grebnev.feature.addmarker.presentation.AddMarkerStore.Intent
import com.grebnev.feature.addmarker.presentation.AddMarkerStore.Label
import com.grebnev.feature.addmarker.presentation.AddMarkerStore.State
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

interface AddMarkerStore : Store<Intent, State, Label> {
    sealed interface Intent {
        data class TitleChanged(
            val title: String,
        ) : Intent

        data class DescriptionChanged(
            val description: String,
        ) : Intent

        data class CameraPositionChanged(
            val position: CameraPosition,
        ) : Intent

        data object SubmitClicked : Intent

        data object BackClicked : Intent
    }

    data class State(
        val title: String,
        val description: String,
        val position: Point,
        val validationErrors: List<ValidationError>,
    ) {
        enum class ValidationError {
            TITLE_EMPTY,
            DESCRIPTION_TOO_LONG,
        }

        val isValid: Boolean get() = validationErrors.isEmpty()
    }

    sealed interface Label {
        data object MarkerAdded : Label

        data object BackClicked : Label
    }
}

class AddMarkerStoreFactory
    @Inject
    constructor(
        private val storeFactory: StoreFactory,
        private val addGeoMarkerUseCase: AddGeoMarkerUseCase,
    ) {
        fun create(): AddMarkerStore =
            object :
                AddMarkerStore,
                Store<Intent, State, Label> by storeFactory
                    .create(
                        name = "AddMarkerStore",
                        initialState =
                            State(
                                title = "",
                                description = "",
                                position =
                                    Point(
                                        Random.nextDouble(56.7, 56.9),
                                        Random.nextDouble(53.1, 53.2),
                                    ),
                                validationErrors = emptyList(),
                            ),
                        executorFactory = ::ExecutorImpl,
                        reducer = ReducerImpl,
                    ) {}

        private sealed interface Action

        private sealed interface Msg {
            data class TitleUpdated(
                val title: String,
            ) : Msg

            data class DescriptionUpdated(
                val description: String,
            ) : Msg

            data class CameraPositionChanged(
                val position: CameraPosition,
            ) : Msg

            data class ValidationFailed(
                val errors: List<State.ValidationError>,
            ) : Msg
        }

        private inner class ExecutorImpl :
            CoroutineExecutor<Intent, Action, State, Msg, Label>() {
            override fun executeIntent(intent: Intent) {
                when (intent) {
                    is Intent.TitleChanged ->
                        dispatch(Msg.TitleUpdated(intent.title))

                    is Intent.DescriptionChanged ->
                        dispatch(Msg.DescriptionUpdated(intent.description))

                    Intent.SubmitClicked -> {
                        submitMarker()
                    }

                    Intent.BackClicked ->
                        publish(Label.BackClicked)

                    is Intent.CameraPositionChanged ->
                        dispatch(Msg.CameraPositionChanged(intent.position))
                }
            }

            private fun submitMarker() {
                val state = state()
                val errors = validate(state)

                if (errors.isNotEmpty()) {
                    dispatch(Msg.ValidationFailed(errors))
                    return
                }

                scope.launch {
                    addGeoMarkerUseCase(
                        GeoMarker(
                            title = state.title,
                            description = state.description,
                            latitude = state.position.latitude,
                            longitude = state.position.longitude,
                        ),
                    )
                    publish(Label.MarkerAdded)
                }
            }
        }

        private fun validate(state: State): List<State.ValidationError> {
            val errors = mutableListOf<State.ValidationError>()
            if (state.title.isBlank()) errors.add(State.ValidationError.TITLE_EMPTY)
            if (state.description.length > 200) errors.add(State.ValidationError.DESCRIPTION_TOO_LONG)
            return errors
        }

        private object ReducerImpl : Reducer<State, Msg> {
            override fun State.reduce(msg: Msg): State =
                when (msg) {
                    is Msg.TitleUpdated -> copy(title = msg.title)
                    is Msg.DescriptionUpdated -> copy(description = msg.description)
                    is Msg.ValidationFailed -> copy(validationErrors = msg.errors)
                    is Msg.CameraPositionChanged -> copy(position = msg.position.target)
                }
        }
    }