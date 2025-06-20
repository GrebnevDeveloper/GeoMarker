package com.grebnev.feature.addmarker.presentation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.ObserveLifecycleMode
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.subscribe
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.grebnev.core.domain.entity.GeoMarker
import com.grebnev.core.extensions.componentScope
import com.grebnev.core.map.presentation.DefaultMapComponentProvider
import com.grebnev.core.map.presentation.MapComponent
import com.grebnev.core.map.presentation.MapStore
import com.grebnev.feature.imagepicker.presentation.DefaultImagePickerComponent
import com.grebnev.feature.imagepicker.presentation.ImagePickerComponent
import com.grebnev.feature.imagepicker.presentation.ImagePickerStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultAddMarkerComponent
    @AssistedInject
    constructor(
        private val addMarkerStoreFactory: AddMarkerStoreFactory,
        private val mapComponentProvider: DefaultMapComponentProvider,
        private val imagePickerComponentFactory: DefaultImagePickerComponent.Factory,
        @Assisted private val onBackClicked: () -> Unit,
        @Assisted private val geoMarker: GeoMarker?,
        @Assisted componentContext: ComponentContext,
    ) : AddMarkerComponent,
        ComponentContext by componentContext {
        private val store =
            if (geoMarker == null) {
                instanceKeeper.getStore { addMarkerStoreFactory.createAddMarkerStore() }
            } else {
                instanceKeeper.getStore { addMarkerStoreFactory.createEditMarkerStore(geoMarker) }
            }

        private val _model = MutableValue(store.stateFlow.value)
        override val model: Value<AddMarkerStore.State> = _model

        init {
            componentScope().launch {
                store.stateFlow.collect { newState ->
                    _model.value = newState
                }
            }
            componentScope().launch {
                store.labels.collect { label ->
                    when (label) {
                        AddMarkerStore.Label.SubmitClicked -> onBackClicked()

                        AddMarkerStore.Label.BackClicked -> onBackClicked()

                        AddMarkerStore.Label.DeleteClicked -> onBackClicked()

                        is AddMarkerStore.Label.AddImagesClicked -> {
                            imagePickerComponent.onIntent(
                                ImagePickerStore.Intent.SyncSelectedImages(label.currentImagesUri),
                            )
                        }

                        is AddMarkerStore.Label.CameraPositionChanged -> {
                            mapComponent.onIntent(MapStore.Intent.UpdateCameraPosition(label.position))
                        }
                    }
                }
            }
        }

        override val mapComponent: MapComponent =
            mapComponentProvider.createLocationPicker(
                cameraPositionChanged = { position ->
                    store.accept(AddMarkerStore.Intent.CameraPositionChanged(position))
                },
                componentContext = childContext("MapComponent"),
            )

        init {
            mapComponent.model.subscribe(
                lifecycle = lifecycle,
                mode = ObserveLifecycleMode.CREATE_DESTROY,
            ) { state ->
                state.cameraPosition?.let {
                    store.accept(AddMarkerStore.Intent.CameraPositionChanged(it))
                }
            }
        }

        override val imagePickerComponent: ImagePickerComponent =
            imagePickerComponentFactory.create(
                onImagesSelectedUri = { imagesUri ->
                    store.accept(AddMarkerStore.Intent.ConfirmImagesSelection(imagesUri))
                },
                onSelectionCanceled = {
                    store.accept(AddMarkerStore.Intent.CancelImagesSelection)
                },
                componentContext = childContext("ImagePickerComponent"),
            )

        override fun onIntent(intent: AddMarkerStore.Intent) {
            store.accept(intent)
        }

        @AssistedFactory
        interface InternalFactory {
            fun create(
                @Assisted onBackClicked: () -> Unit,
                @Assisted geoMarker: GeoMarker?,
                @Assisted componentContext: ComponentContext,
            ): DefaultAddMarkerComponent
        }
    }

class DefaultAddMarkerComponentProvider
    @Inject
    constructor(
        private val factory: DefaultAddMarkerComponent.InternalFactory,
    ) {
        fun createAddMarker(
            onBackClicked: () -> Unit,
            componentContext: ComponentContext,
        ): DefaultAddMarkerComponent =
            factory.create(
                onBackClicked = onBackClicked,
                geoMarker = null,
                componentContext = componentContext,
            )

        fun createEditMarker(
            onBackClicked: () -> Unit,
            geoMarker: GeoMarker,
            componentContext: ComponentContext,
        ): DefaultAddMarkerComponent =
            factory.create(
                onBackClicked = onBackClicked,
                geoMarker = geoMarker,
                componentContext = componentContext,
            )
    }