package com.grebnev.feature.editormarker.presentation

import android.net.Uri
import androidx.core.net.toUri
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.grebnev.core.domain.entity.GeoMarker
import com.grebnev.core.map.extensions.defaultCameraPosition
import com.grebnev.feature.editormarker.domain.DeleteMarkerUseCase
import com.grebnev.feature.editormarker.domain.SaveGeoMarkerUseCase
import com.grebnev.feature.editormarker.presentation.EditorMarkerStore.Intent
import com.grebnev.feature.editormarker.presentation.EditorMarkerStore.Label
import com.grebnev.feature.editormarker.presentation.EditorMarkerStore.State
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

interface EditorMarkerStore : Store<Intent, State, Label> {
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

        data object SaveClicked : Intent

        data object BackClicked : Intent

        data object DeleteClicked : Intent

        data class AddImagesClicked(
            val currentImagesUri: List<Uri>,
        ) : Intent

        data class ConfirmImagesSelection(
            val imagesUri: List<Uri>,
        ) : Intent

        data object CancelImagesSelection : Intent

        data class RemoveImage(
            val imageUri: Uri,
        ) : Intent
    }

    data class State(
        val editorMode: EditorMode,
        val title: String,
        val description: String,
        val location: CameraPosition?,
        val validationErrors: List<ValidationError>,
        val selectedImages: List<Uri>,
        val showImagePicker: Boolean,
        val markerId: Long?,
    ) {
        enum class ValidationError {
            TITLE_EMPTY,
            DESCRIPTION_TOO_LONG,
        }

        val isValid: Boolean get() = validationErrors.isEmpty()
    }

    sealed interface Label {
        data object SaveClicked : Label

        data object BackClicked : Label

        data object DeleteClicked : Label

        data class AddImagesClicked(
            val currentImagesUri: List<Uri>,
        ) : Label

        data class CameraPositionChanged(
            val position: CameraPosition,
        ) : Label
    }
}

class EditorMarkerStoreFactory
    @Inject
    constructor(
        private val storeFactory: StoreFactory,
        private val saveGeoMarkerUseCase: SaveGeoMarkerUseCase,
        private val deleteMarkerUseCase: DeleteMarkerUseCase,
    ) {
        fun createAddMarkerStore(): EditorMarkerStore =
            object :
                EditorMarkerStore,
                Store<Intent, State, Label> by storeFactory
                    .create(
                        name = "AddMarkerStore",
                        initialState =
                            State(
                                editorMode = EditorMode.ADD_MARKER,
                                title = "",
                                description = "",
                                location = null,
                                validationErrors = emptyList(),
                                selectedImages = emptyList(),
                                showImagePicker = false,
                                markerId = null,
                            ),
                        executorFactory = ::ExecutorImpl,
                        reducer = ReducerImpl,
                    ) {}

        fun createEditMarkerStore(geoMarker: GeoMarker): EditorMarkerStore =
            object :
                EditorMarkerStore,
                Store<Intent, State, Label> by storeFactory
                    .create(
                        name = "EditMarkerStore",
                        initialState =
                            State(
                                editorMode = EditorMode.EDIT_MARKER,
                                title = geoMarker.title,
                                description = geoMarker.description,
                                location = null,
                                validationErrors = emptyList(),
                                selectedImages = geoMarker.imagesUri.map { it.toUri() },
                                showImagePicker = false,
                                markerId = geoMarker.id,
                            ),
                        executorFactory = ::ExecutorImpl,
                        bootstrapper = BootstrapperImpl(geoMarker),
                        reducer = ReducerImpl,
                    ) {}

        private sealed interface Action {
            data class CameraPositionChanged(
                val position: CameraPosition,
            ) : Action
        }

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

            data class ImagesSelected(
                val imagesUri: List<Uri>,
            ) : Msg

            data class ShowImagePicker(
                val isShow: Boolean,
            ) : Msg

            data class ImageRemoved(
                val imageUri: Uri,
            ) : Msg
        }

        private inner class BootstrapperImpl(
            private val marker: GeoMarker,
        ) : CoroutineBootstrapper<Action>() {
            override fun invoke() {
                scope.launch {
                    delay(TIMEOUT_SETTING_POSITION_MARKER)
                    dispatch(
                        Action.CameraPositionChanged(
                            Point(marker.latitude, marker.longitude).defaultCameraPosition,
                        ),
                    )
                }
            }
        }

        private inner class ExecutorImpl : CoroutineExecutor<Intent, Action, State, Msg, Label>() {
            override fun executeAction(action: Action) {
                when (action) {
                    is Action.CameraPositionChanged -> {
                        dispatch(Msg.CameraPositionChanged(action.position))
                        publish(Label.CameraPositionChanged(action.position))
                    }
                }
            }

            override fun executeIntent(intent: Intent) {
                when (intent) {
                    is Intent.TitleChanged ->
                        dispatch(Msg.TitleUpdated(intent.title))

                    is Intent.DescriptionChanged ->
                        dispatch(Msg.DescriptionUpdated(intent.description))

                    Intent.SaveClicked -> {
                        saveMarker()
                    }

                    Intent.BackClicked ->
                        publish(Label.BackClicked)

                    is Intent.CameraPositionChanged ->
                        dispatch(Msg.CameraPositionChanged(intent.position))

                    is Intent.AddImagesClicked -> {
                        dispatch(Msg.ShowImagePicker(true))
                        publish(Label.AddImagesClicked(intent.currentImagesUri))
                    }

                    Intent.CancelImagesSelection ->
                        dispatch(Msg.ShowImagePicker(false))

                    is Intent.ConfirmImagesSelection -> {
                        dispatch(Msg.ImagesSelected(intent.imagesUri))
                        dispatch(Msg.ShowImagePicker(false))
                    }

                    is Intent.RemoveImage ->
                        dispatch(Msg.ImageRemoved(intent.imageUri))

                    Intent.DeleteClicked -> {
                        val state = state()
                        scope.launch {
                            if (state.editorMode == EditorMode.EDIT_MARKER && state.markerId != null) {
                                deleteMarkerUseCase(state.markerId)
                                publish(Label.DeleteClicked)
                            }
                        }
                    }
                }
            }

            private fun saveMarker() {
                val state = state()
                val errors = validate(state)

                if (errors.isNotEmpty()) {
                    dispatch(Msg.ValidationFailed(errors))
                    return
                }

                scope.launch {
                    if (state.location != null) {
                        if (state.editorMode == EditorMode.EDIT_MARKER && state.markerId != null) {
                            saveGeoMarkerUseCase(
                                GeoMarker(
                                    id = state.markerId,
                                    title = state.title,
                                    description = state.description,
                                    latitude = state.location.target.latitude,
                                    longitude = state.location.target.longitude,
                                    imagesUri = state.selectedImages.map { it.toString() },
                                ),
                            )
                        } else {
                            saveGeoMarkerUseCase(
                                GeoMarker(
                                    title = state.title,
                                    description = state.description,
                                    latitude = state.location.target.latitude,
                                    longitude = state.location.target.longitude,
                                    imagesUri = state.selectedImages.map { it.toString() },
                                ),
                            )
                        }
                        publish(Label.SaveClicked)
                    }
                }
            }
        }

        private fun validate(state: State): List<State.ValidationError> {
            val errors = mutableListOf<State.ValidationError>()
            if (state.title.isBlank()) errors.add(State.ValidationError.TITLE_EMPTY)
            if (state.description.length > MAX_LENGTH_DESCRIPTION) {
                errors.add(State.ValidationError.DESCRIPTION_TOO_LONG)
            }
            return errors
        }

        private object ReducerImpl : Reducer<State, Msg> {
            override fun State.reduce(msg: Msg): State =
                when (msg) {
                    is Msg.TitleUpdated -> copy(title = msg.title)
                    is Msg.DescriptionUpdated -> copy(description = msg.description)
                    is Msg.ValidationFailed -> copy(validationErrors = msg.errors)
                    is Msg.CameraPositionChanged -> copy(location = msg.position)
                    is Msg.ImagesSelected -> copy(selectedImages = msg.imagesUri)
                    is Msg.ShowImagePicker -> copy(showImagePicker = msg.isShow)
                    is Msg.ImageRemoved -> copy(selectedImages = selectedImages - msg.imageUri)
                }
        }

        companion object {
            private const val MAX_LENGTH_DESCRIPTION = 200L
            private const val TIMEOUT_SETTING_POSITION_MARKER = 100L
        }
    }