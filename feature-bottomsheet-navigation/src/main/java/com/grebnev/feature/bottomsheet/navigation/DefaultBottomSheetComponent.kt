package com.grebnev.feature.bottomsheet.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackCallback
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.grebnev.core.common.extensions.scope
import com.grebnev.core.domain.entity.GeoMarker
import com.grebnev.feature.bottomsheet.navigation.BottomSheetComponent.Child.DetailsMarker
import com.grebnev.feature.bottomsheet.navigation.BottomSheetComponent.Child.ListMarkers
import com.grebnev.feature.detailsmarker.presentation.DefaultDetailsMarkerComponent
import com.grebnev.feature.geomarker.api.GeoMarkerStore
import com.grebnev.feature.listmarkers.DefaultListMarkersComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@OptIn(DelicateDecomposeApi::class, ExperimentalCoroutinesApi::class)
class DefaultBottomSheetComponent
    @AssistedInject
    constructor(
        private val listMarkersComponentFactory: DefaultListMarkersComponent.Factory,
        private val detailsMarkerComponentFactory: DefaultDetailsMarkerComponent.Factory,
        @Assisted("onMarkerSelected") private val onMarkerSelected: (GeoMarker?) -> Unit,
        @Assisted("onEditMarkerClicked") private val onEditMarkerClicked: (GeoMarker) -> Unit,
        @Assisted("geoMarkerStore") private val geoMarkerStore: GeoMarkerStore,
        @Assisted("componentContext") component: ComponentContext,
    ) : BottomSheetComponent,
        ComponentContext by component {
        private val navigation = StackNavigation<Config>()

        override val stack: Value<ChildStack<*, BottomSheetComponent.Child>> =
            childStack(
                source = navigation,
                serializer = Config.serializer(),
                initialConfiguration = Config.ListMarkers,
                handleBackButton = true,
                childFactory = ::createChild,
            )

        private var backCallback: BackCallback? = null

        init {
            scope.launch {
                geoMarkerStore.stateFlow.collect { state ->
                    handleMarkerSelection(state.selectedMarker)
                }
            }
            scope.launch {
                stack.subscribe { childStack ->
                    updateBackCallback(childStack.active.configuration)
                }
            }
        }

        private fun updateBackCallback(config: Any) {
            when (config as? Config) {
                is Config.DetailsMarker -> {
                    if (backCallback == null) {
                        backCallback =
                            BackCallback {
                                onMarkerSelected(null)
                                navigation.pop()
                            }.also {
                                backHandler.register(it)
                            }
                    }
                }

                else -> {
                    backCallback?.let {
                        backHandler.unregister(it)
                        backCallback = null
                    }
                }
            }
        }

        private fun handleMarkerSelection(marker: GeoMarker?) {
            val currentStack = stack.value
            val currentTop = currentStack.active.configuration

            when {
                marker == null -> {
                    if (currentTop is Config.DetailsMarker) {
                        navigation.pop()
                    }
                }
                currentTop is Config.DetailsMarker && currentTop.marker == marker -> {
                    return
                }
                currentTop is Config.DetailsMarker -> {
                    navigation.replaceCurrent(Config.DetailsMarker(marker))
                }
                else -> {
                    navigation.push(Config.DetailsMarker(marker))
                }
            }
        }

        private fun createChild(
            config: Config,
            componentContext: ComponentContext,
        ): BottomSheetComponent.Child =
            when (config) {
                is Config.ListMarkers -> {
                    val component =
                        listMarkersComponentFactory.create(
                            geoMarkerStore = geoMarkerStore,
                            onMarkerSelected = { marker ->
                                onMarkerSelected(marker)
                            },
                            component = componentContext,
                        )
                    ListMarkers(component)
                }

                is Config.DetailsMarker -> {
                    val component =
                        detailsMarkerComponentFactory.create(
                            marker = config.marker,
                            onBackClicked = {
                                onMarkerSelected(null)
                            },
                            onEditClicked = {
                                onEditMarkerClicked(config.marker)
                            },
                            component = componentContext,
                        )
                    DetailsMarker(component)
                }
            }

        @Serializable
        sealed interface Config {
            @Serializable
            data object ListMarkers : Config

            @Serializable
            data class DetailsMarker(
                val marker: GeoMarker,
            ) : Config
        }

        @AssistedFactory
        interface Factory {
            fun create(
                @Assisted("geoMarkerStore") geoMarkerStore: GeoMarkerStore,
                @Assisted("onMarkerSelected") onMarkerSelected: (GeoMarker?) -> Unit,
                @Assisted("onEditMarkerClicked") onEditMarkerClicked: (GeoMarker) -> Unit,
                @Assisted("componentContext") componentContext: ComponentContext,
            ): DefaultBottomSheetComponent
        }
    }