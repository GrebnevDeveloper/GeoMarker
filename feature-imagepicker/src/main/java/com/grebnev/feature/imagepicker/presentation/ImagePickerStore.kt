package com.grebnev.feature.imagepicker.presentation

import android.net.Uri
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.grebnev.core.gallery.domain.usecase.GetImagesUriUseCase
import com.grebnev.core.gallery.domain.usecase.GetPhotoUriUseCase
import com.grebnev.feature.imagepicker.presentation.ImagePickerStore.Intent
import com.grebnev.feature.imagepicker.presentation.ImagePickerStore.Label
import com.grebnev.feature.imagepicker.presentation.ImagePickerStore.State
import kotlinx.coroutines.launch
import javax.inject.Inject

interface ImagePickerStore : Store<Intent, State, Label> {
    sealed interface Intent {
        data class SyncSelectedImages(
            val currentImagesUri: List<Uri>,
        ) : Intent

        class ImageClicked(
            val imageUri: Uri,
        ) : Intent

        data object OpenCameraClicked : Intent

        data class PhotoTaken(
            val result: Boolean,
        ) : Intent

        data object ConfirmClicked : Intent

        data object CancelClicked : Intent
    }

    data class State(
        val availableImagesUri: List<Uri>,
        val selectedImagesUri: List<Uri>,
        val photoUri: Uri?,
    )

    sealed interface Label {
        data object ImagesConfirmed : Label

        data object CameraOpened : Label

        data object SelectionCancelled : Label
    }
}

class ImagePickerStoreFactory
    @Inject
    constructor(
        private val storeFactory: StoreFactory,
        private val getImagesUriUseCase: GetImagesUriUseCase,
        private val getPhotoUriUseCase: GetPhotoUriUseCase,
    ) {
        fun create(): ImagePickerStore =
            object :
                ImagePickerStore,
                Store<Intent, State, Label> by storeFactory.create(
                    name = "ImagePickerStore",
                    initialState =
                        State(
                            availableImagesUri = emptyList(),
                            selectedImagesUri = emptyList(),
                            photoUri = null,
                        ),
                    bootstrapper = BootstrapperImpl(),
                    executorFactory = ::ExecutorImpl,
                    reducer = ReducerImpl,
                ) {}

        private sealed interface Action {
            data class ImagesLoaded(
                val imagesUri: List<Uri>,
            ) : Action

            data class PhotoUriPrepared(
                val photoUri: Uri,
            ) : Action
        }

        private sealed interface Msg {
            data class ImagesLoaded(
                val imagesUri: List<Uri>,
            ) : Msg

            data class ImageToggled(
                val imageUri: Uri,
                val isSelected: Boolean,
            ) : Msg

            data class SelectedImagesSynced(
                val currentImagesUri: List<Uri>,
            ) : Msg

            data class TakenPhotoAdded(
                val photoUri: Uri,
            ) : Msg

            data class PhotoUriPrepared(
                val photoUri: Uri,
            ) : Msg
        }

        private inner class BootstrapperImpl : CoroutineBootstrapper<Action>() {
            override fun invoke() {
                scope.launch {
                    val photoUri = getPhotoUriUseCase()
                    photoUri?.let {
                        dispatch(Action.PhotoUriPrepared(photoUri))
                    }

                    val imagesUri = getImagesUriUseCase()
                    dispatch(Action.ImagesLoaded(imagesUri))
                }
            }
        }

        private inner class ExecutorImpl : CoroutineExecutor<Intent, Action, State, Msg, Label>() {
            override fun executeIntent(intent: Intent) {
                when (intent) {
                    is Intent.ImageClicked -> {
                        val currentState = state()
                        val isSelected = currentState.selectedImagesUri.contains(intent.imageUri)
                        dispatch(Msg.ImageToggled(intent.imageUri, !isSelected))
                    }
                    Intent.OpenCameraClicked -> {
                        scope.launch {
                            openCamera()
                        }
                    }
                    Intent.ConfirmClicked ->
                        publish(Label.ImagesConfirmed)
                    Intent.CancelClicked ->
                        publish(Label.SelectionCancelled)

                    is Intent.SyncSelectedImages ->
                        dispatch(Msg.SelectedImagesSynced(intent.currentImagesUri))

                    is Intent.PhotoTaken -> {
                        if (intent.result) {
                            state().photoUri?.let {
                                dispatch(Msg.TakenPhotoAdded(it))
                            }
                        }
                    }
                }
            }

            private suspend fun openCamera() {
                val imageUri = getPhotoUriUseCase()
                imageUri?.let {
                    dispatch(Msg.TakenPhotoAdded(it))
                }
            }

            override fun executeAction(action: Action) {
                when (action) {
                    is Action.ImagesLoaded ->
                        dispatch(Msg.ImagesLoaded(action.imagesUri))

                    is Action.PhotoUriPrepared -> {
                        dispatch(Msg.PhotoUriPrepared(action.photoUri))
                    }
                }
            }
        }

        private object ReducerImpl : Reducer<State, Msg> {
            override fun State.reduce(msg: Msg): State =
                when (msg) {
                    is Msg.ImagesLoaded -> copy(availableImagesUri = msg.imagesUri)
                    is Msg.ImageToggled -> {
                        if (msg.isSelected) {
                            copy(selectedImagesUri = selectedImagesUri + msg.imageUri)
                        } else {
                            copy(selectedImagesUri = selectedImagesUri - msg.imageUri)
                        }
                    }

                    is Msg.SelectedImagesSynced -> copy(selectedImagesUri = msg.currentImagesUri)
                    is Msg.TakenPhotoAdded ->
                        copy(
                            availableImagesUri = availableImagesUri + msg.photoUri,
                            selectedImagesUri = selectedImagesUri + msg.photoUri,
                        )

                    is Msg.PhotoUriPrepared -> copy(photoUri = msg.photoUri)
                }
        }
    }