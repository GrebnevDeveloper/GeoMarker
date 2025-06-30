package com.grebnev.geomarker.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import com.grebnev.core.domain.entity.GeoMarker
import com.grebnev.feature.editormarker.presentation.DefaultAddMarkerComponentProvider
import com.grebnev.feature.geomarker.presentation.DefaultGeoMarkerComponent
import com.grebnev.geomarker.navigation.RootComponent.Child.AddMarker
import com.grebnev.geomarker.navigation.RootComponent.Child.EditMarker
import com.grebnev.geomarker.navigation.RootComponent.Child.GeoMarkers
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.serialization.Serializable

@OptIn(DelicateDecomposeApi::class)
class DefaultRootComponent
    @AssistedInject
    constructor(
        private val geoMarkerComponentFactory: DefaultGeoMarkerComponent.Factory,
        private val addMarkerComponentProvider: DefaultAddMarkerComponentProvider,
        @Assisted componentContext: ComponentContext,
    ) : RootComponent,
        ComponentContext by componentContext {
        private val navigation = StackNavigation<Config>()

        override val stack: Value<ChildStack<*, RootComponent.Child>> =
            childStack(
                source = navigation,
                serializer = Config.serializer(),
                initialConfiguration = Config.GeoMarkers,
                handleBackButton = true,
                childFactory = ::createChild,
            )

        private fun createChild(
            config: Config,
            componentContext: ComponentContext,
        ): RootComponent.Child =
            when (config) {
                Config.GeoMarkers -> {
                    val component =
                        geoMarkerComponentFactory.create(
                            onAddMarkerClicked = {
                                navigation.push(Config.AddMarker)
                            },
                            onEditMarkerClicked = { marker ->
                                navigation.push(Config.EditMarker(marker))
                            },
                            componentContext = componentContext,
                        )
                    GeoMarkers(component)
                }
                is Config.AddMarker -> {
                    val component =
                        addMarkerComponentProvider.createAddMarker(
                            onBackClicked = {
                                navigation.pop()
                            },
                            componentContext = componentContext,
                        )
                    AddMarker(component)
                }

                is Config.EditMarker -> {
                    val component =
                        addMarkerComponentProvider.createEditMarker(
                            onBackClicked = {
                                navigation.pop()
                            },
                            geoMarker = config.geoMarker,
                            componentContext = componentContext,
                        )
                    EditMarker(component)
                }
            }

        @Serializable
        sealed interface Config {
            @Serializable
            data object GeoMarkers : Config

            @Serializable
            data object AddMarker : Config

            @Serializable
            data class EditMarker(
                val geoMarker: GeoMarker,
            ) : Config
        }

        @AssistedFactory
        interface Factory {
            fun create(
                @Assisted componentContext: ComponentContext,
            ): DefaultRootComponent
        }
    }