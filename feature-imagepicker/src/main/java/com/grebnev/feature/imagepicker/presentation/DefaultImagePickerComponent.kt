package com.grebnev.feature.imagepicker.presentation

import android.net.Uri
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.grebnev.core.extensions.componentScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultImagePickerComponent
    @AssistedInject
    constructor(
        private val imagePickerStoreFactory: ImagePickerStoreFactory,
        @Assisted private val onImagesSelectedUri: (List<Uri>) -> Unit,
        @Assisted private val onSelectionCancelled: () -> Unit,
        @Assisted componentContext: ComponentContext,
    ) : ImagePickerComponent,
        ComponentContext by componentContext {
        private val store = instanceKeeper.getStore { imagePickerStoreFactory.create() }

        private val _model = MutableValue(store.stateFlow.value)
        override val model: Value<ImagePickerStore.State> = _model

        private val scope = componentScope()

        init {
            lifecycle.doOnCreate {
            }
            scope.launch {
                store.stateFlow.collect { newState ->
                    _model.value = newState
                }
            }

            scope.launch {
                store.labels.collect { label ->
                    when (label) {
                        ImagePickerStore.Label.ImagesConfirmed ->
                            onImagesSelectedUri(model.value.selectedImagesUri)

                        ImagePickerStore.Label.SelectionCancelled ->
                            onSelectionCancelled()
                    }
                }
            }
        }

        override fun onIntent(intent: ImagePickerStore.Intent) {
            store.accept(intent)
        }

        @AssistedFactory
        interface Factory {
            fun create(
                @Assisted onImagesSelectedUri: (List<Uri>) -> Unit,
                @Assisted onSelectionCanceled: () -> Unit,
                @Assisted componentContext: ComponentContext,
            ): DefaultImagePickerComponent
        }
    }