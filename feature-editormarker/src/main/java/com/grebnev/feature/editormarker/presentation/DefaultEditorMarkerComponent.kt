package com.grebnev.feature.editormarker.presentation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.value.ObserveLifecycleMode
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.subscribe
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.grebnev.core.common.delegates.StateFlowDelegate
import com.grebnev.core.common.extensions.scope
import com.grebnev.core.domain.entity.GeoMarker
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
class DefaultEditorMarkerComponent
    @AssistedInject
    constructor(
        private val editorMarkerStoreFactory: EditorMarkerStoreFactory,
        private val mapComponentProvider: DefaultMapComponentProvider,
        private val imagePickerComponentFactory: DefaultImagePickerComponent.Factory,
        @Assisted private val onBackClicked: () -> Unit,
        @Assisted private val geoMarker: GeoMarker?,
        @Assisted componentContext: ComponentContext,
    ) : EditorMarkerComponent,
        ComponentContext by componentContext {
        private val store =
            if (geoMarker == null) {
                instanceKeeper.getStore { editorMarkerStoreFactory.createAddMarkerStore() }
            } else {
                instanceKeeper.getStore { editorMarkerStoreFactory.createEditMarkerStore(geoMarker) }
            }

        override val model: Value<EditorMarkerStore.State> by StateFlowDelegate(scope, store.stateFlow)

        init {
            scope.launch {
                store.labels.collect { label ->
                    when (label) {
                        EditorMarkerStore.Label.SaveClicked -> onBackClicked()

                        EditorMarkerStore.Label.BackClicked -> onBackClicked()

                        EditorMarkerStore.Label.DeleteClicked -> onBackClicked()

                        is EditorMarkerStore.Label.AddImagesClicked -> {
                            imagePickerComponent.onIntent(
                                ImagePickerStore.Intent.SyncSelectedImages(label.currentImagesUri),
                            )
                        }

                        is EditorMarkerStore.Label.CameraPositionChanged -> {
                            mapComponent.onIntent(MapStore.Intent.UpdateCameraPosition(label.position))
                        }
                    }
                }
            }
        }

        override val mapComponent: MapComponent =
            mapComponentProvider.createLocationPicker(
                cameraPositionChanged = { position ->
                    store.accept(EditorMarkerStore.Intent.CameraPositionChanged(position))
                },
                componentContext = childContext("MapComponent"),
            )

        init {
            mapComponent.model.subscribe(
                lifecycle = lifecycle,
                mode = ObserveLifecycleMode.CREATE_DESTROY,
            ) { state ->
                state.cameraPosition?.let {
                    store.accept(EditorMarkerStore.Intent.CameraPositionChanged(it))
                }
            }
        }

        override val imagePickerComponent: ImagePickerComponent =
            imagePickerComponentFactory.create(
                onImagesSelectedUri = { imagesUri ->
                    store.accept(EditorMarkerStore.Intent.ConfirmImagesSelection(imagesUri))
                },
                onSelectionCanceled = {
                    store.accept(EditorMarkerStore.Intent.CancelImagesSelection)
                },
                componentContext = childContext("ImagePickerComponent"),
            )

        override fun onIntent(intent: EditorMarkerStore.Intent) {
            store.accept(intent)
        }

        @AssistedFactory
        interface InternalFactory {
            fun create(
                @Assisted onBackClicked: () -> Unit,
                @Assisted geoMarker: GeoMarker?,
                @Assisted componentContext: ComponentContext,
            ): DefaultEditorMarkerComponent
        }
    }

class DefaultAddMarkerComponentProvider
    @Inject
    constructor(
        private val factory: DefaultEditorMarkerComponent.InternalFactory,
    ) {
        fun createAddMarker(
            onBackClicked: () -> Unit,
            componentContext: ComponentContext,
        ): DefaultEditorMarkerComponent =
            factory.create(
                onBackClicked = onBackClicked,
                geoMarker = null,
                componentContext = componentContext,
            )

        fun createEditMarker(
            onBackClicked: () -> Unit,
            geoMarker: GeoMarker,
            componentContext: ComponentContext,
        ): DefaultEditorMarkerComponent =
            factory.create(
                onBackClicked = onBackClicked,
                geoMarker = geoMarker,
                componentContext = componentContext,
            )
    }