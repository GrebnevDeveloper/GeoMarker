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
import com.grebnev.core.domain.entity.GeoMarker
import com.grebnev.core.extensions.componentScope
import com.grebnev.feature.bottomsheet.navigation.BottomSheetComponent.Child.DetailsMarker
import com.grebnev.feature.bottomsheet.navigation.BottomSheetComponent.Child.ListMarkers
import com.grebnev.feature.detailsmarker.presentation.DefaultDetailsMarkerComponent
import com.grebnev.feature.listmarkers.DefaultListMarkersComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@OptIn(DelicateDecomposeApi::class)
class DefaultBottomSheetComponent
    @AssistedInject
    constructor(
        private val listMarkersComponentFactory: DefaultListMarkersComponent.Factory,
        private val detailsMarkerComponentFactory: DefaultDetailsMarkerComponent.Factory,
        @Assisted private val onMarkerSelected: (Long?) -> Unit,
        @Assisted markersFlow: Flow<List<GeoMarker>>,
        @Assisted selectedMarkerIdFlow: StateFlow<Long?>,
        @Assisted component: ComponentContext,
    ) : BottomSheetComponent,
        ComponentContext by component {
        private val navigation = StackNavigation<Config>()

        private val scope = componentScope()

        override val stack: Value<ChildStack<*, BottomSheetComponent.Child>> =
            childStack(
                source = navigation,
                serializer = Config.serializer(),
                initialConfiguration = Config.ListMarkers(markersFlow),
                handleBackButton = true,
                childFactory = ::createChild,
            )

        private var backCallback: BackCallback? = null

        init {
            scope.launch {
                selectedMarkerIdFlow.collect { markerId ->
                    handleMarkerSelection(markerId)
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

        private fun handleMarkerSelection(markerId: Long?) {
            val currentStack = stack.value
            val currentTop = currentStack.active.configuration

            when {
                markerId == null -> {
                    if (currentTop is Config.DetailsMarker) {
                        navigation.pop()
                    }
                }
                currentTop is Config.DetailsMarker && currentTop.markerId == markerId -> {
                    return
                }
                currentTop is Config.DetailsMarker -> {
                    navigation.replaceCurrent(Config.DetailsMarker(markerId))
                }
                else -> {
                    navigation.push(Config.DetailsMarker(markerId))
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
                            markersFlow = config.markersFlow,
                            onMarkerSelected = { marker ->
                                onMarkerSelected(marker.id)
                            },
                            component = componentContext,
                        )
                    ListMarkers(component)
                }

                is Config.DetailsMarker -> {
                    val component =
                        detailsMarkerComponentFactory.create(
                            markerId = config.markerId,
                            onBackClicked = {
                                onMarkerSelected(null)
                            },
                            component = componentContext,
                        )
                    DetailsMarker(component)
                }
            }

        @Serializable
        sealed interface Config {
            @Serializable
            data class ListMarkers(
                val markersFlow: Flow<List<GeoMarker>>,
            ) : Config

            @Serializable
            data class DetailsMarker(
                val markerId: Long,
            ) : Config
        }

        @AssistedFactory
        interface Factory {
            fun create(
                @Assisted markersFlow: Flow<List<GeoMarker>>,
                @Assisted selectedMarkerIdFlow: StateFlow<Long?>,
                @Assisted onMarkerSelected: (Long?) -> Unit,
                @Assisted componentContext: ComponentContext,
            ): DefaultBottomSheetComponent
        }
    }