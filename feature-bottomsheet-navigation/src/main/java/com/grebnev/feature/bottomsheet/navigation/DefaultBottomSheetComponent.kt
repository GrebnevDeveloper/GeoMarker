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
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

@OptIn(DelicateDecomposeApi::class)
class DefaultBottomSheetComponent
    @AssistedInject
    constructor(
        private val listMarkersComponentFactory: DefaultListMarkersComponent.Factory,
        @Assisted markersFlow: Flow<List<GeoMarker>>,
        @Assisted component: ComponentContext,
    ) : BottomSheetComponent,
        ComponentContext by component {
        private val navigation = StackNavigation<Config>()

        override val stack: Value<ChildStack<*, BottomSheetComponent.Child>> =
            childStack(
                source = navigation,
                serializer = Config.serializer(),
                initialConfiguration = Config.ListMarkers(markersFlow),
                handleBackButton = true,
                childFactory = ::createChild,
            )

        private fun createChild(
            config: Config,
            componentContext: ComponentContext,
        ): BottomSheetComponent.Child =
            when (config) {
                is Config.ListMarkers -> {
                    val component =
                        listMarkersComponentFactory.create(
                            markersFlow = config.markersFlow,
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
            data class ListMarkers(
                val markersFlow: Flow<List<GeoMarker>>,
            ) : Config
        }

        @AssistedFactory
        interface Factory {
            fun create(
                @Assisted markersFlow: Flow<List<GeoMarker>>,
                @Assisted componentContext: ComponentContext,
            ): DefaultBottomSheetComponent
        }
    }