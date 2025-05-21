package com.grebnev.geomarker.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import com.grebnev.feature.addmarker.DefaultAddMarkerComponent
import com.grebnev.feature.geomarker.DefaultGeoMarkerComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.serialization.Serializable

@OptIn(DelicateDecomposeApi::class)
class DefaultRootComponent
    @AssistedInject
    constructor(
        private val geoMarkerComponentFactory: DefaultGeoMarkerComponent.Factory,
        private val addMarkerComponentFactory: DefaultAddMarkerComponent.Factory,
        @Assisted componentContext: ComponentContext,
    ) : RootComponent,
        ComponentContext by componentContext {
        private val navigation = StackNavigation<Config>()

        override val stack: Value<ChildStack<*, RootComponent.Child>> =
            childStack(
                source = navigation,
                serializer = Config.serializer(),
                initialConfiguration = Config.GeoMarker,
                handleBackButton = true,
                childFactory = ::createChild,
            )

        private fun createChild(
            config: Config,
            componentContext: ComponentContext,
        ): RootComponent.Child =
            when (config) {
                Config.GeoMarker -> {
                    val component =
                        geoMarkerComponentFactory.create(
                            onAddMarkerClicked = {
                                navigation.push(Config.AddMarker)
                            },
                            componentContext = componentContext,
                        )
                    RootComponent.Child.GeoMarker(component)
                }
                Config.AddMarker -> {
                    val component =
                        addMarkerComponentFactory.create(
                            onBackClicked = {
                                navigation.pop()
                            },
                            componentContext = componentContext,
                        )
                    RootComponent.Child.AddMarker(component)
                }
            }

        @Serializable
        sealed interface Config {
            @Serializable
            data object GeoMarker : Config

            @Serializable
            data object AddMarker : Config
        }

        @AssistedFactory
        interface Factory {
            fun create(
                @Assisted componentContext: ComponentContext,
            ): DefaultRootComponent
        }
    }