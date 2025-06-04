package com.grebnev.feature.imagepicker.presentation

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.grebnev.feature.imagepicker.presentation.ImagePickerStore.Intent
import com.grebnev.feature.imagepicker.presentation.ImagePickerStore.Label
import com.grebnev.feature.imagepicker.presentation.ImagePickerStore.State
import javax.inject.Inject

interface ImagePickerStore : Store<Intent, State, Label> {
    sealed interface Intent {
        data class ImageClicked(
            val imageUri: String,
        ) : Intent

        data object OpenCameraClicked : Intent

        data object ConfirmClicked : Intent

        data object CancelClicked : Intent
    }

    data class State(
        val availableImagesUri: List<String>,
        val selectedImagesUri: List<String>,
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
                        ),
                    executorFactory = ::ExecutorImpl,
                    reducer = ReducerImpl,
                ) {}

        private sealed interface Action

        private sealed interface Msg {
            data class ImagesLoaded(
                val imagesUri: List<String>,
            ) : Msg

            data class ImageToggled(
                val imageUri: String,
                val isSelected: Boolean,
            ) : Msg
        }

        private inner class ExecutorImpl : CoroutineExecutor<Intent, Action, State, Msg, Label>() {
            override fun executeIntent(intent: Intent) {
                when (intent) {
                    is Intent.ImageClicked -> toggleImage(intent.imageUri)
                    Intent.OpenCameraClicked -> openCamera()
                    Intent.ConfirmClicked -> confirmSelection()
                    Intent.CancelClicked -> cancelSelection()
                }
            }

            private fun toggleImage(imageUri: String) {
                val currentState = state()
                val isSelected = currentState.selectedImagesUri.contains(imageUri)
                dispatch(Msg.ImageToggled(imageUri, !isSelected))
            }

            private fun openCamera() {
            }

            private fun confirmSelection() {
                publish(Label.ImagesConfirmed)
            }

            private fun cancelSelection() {
                publish(Label.SelectionCancelled)
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
                }
        }
    }