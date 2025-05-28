package com.grebnev.feature.bottomsheet.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.value.Value
import com.grebnev.core.domain.entity.GeoMarker
import com.grebnev.feature.listmarkers.DefaultListMarkersComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.serialization.Serializable

@OptIn(DelicateDecomposeApi::class)
class DefaultBottomSheetComponent
    @AssistedInject
    constructor(
        private val listMarkersComponentFactory: DefaultListMarkersComponent.Factory,
        @Assisted private val markers: List<GeoMarker>,
        @Assisted private val selectedMarker: GeoMarker?,
        @Assisted component: ComponentContext,
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

        private fun createChild(
            config: Config,
            componentContext: ComponentContext,
        ): BottomSheetComponent.Child =
            when (config) {
                Config.ListMarkers -> {
                    val component =
                        listMarkersComponentFactory.create(
                            markers = markers,
                            onMarkerSelected = { markerId ->
                                navigateToDetail(markerId)
                            },
                            component = componentContext,
                        )
                    BottomSheetComponent.Child.ListMarkers(component)
                }
            }

        override fun navigateToDetail(markerId: Long) {
        }

        override fun onBackPressed(): Boolean =
            if (stack.value.backStack.isNotEmpty()) {
                navigation.pop()
                true
            } else {
                false
            }

        @Serializable
        sealed interface Config {
            @Serializable
            data object ListMarkers : Config
        }

        @AssistedFactory
        interface Factory {
            fun create(
                @Assisted markers: List<GeoMarker>,
                @Assisted selectedMarker: GeoMarker?,
                @Assisted componentContext: ComponentContext,
            ): DefaultBottomSheetComponent
        }
    }