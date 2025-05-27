package com.grebnev.feature.bottomsheet.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import com.grebnev.core.domain.entity.GeoMarker
import com.grebnev.feature.listmarkers.ListMarkersComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

@OptIn(DelicateDecomposeApi::class)
class DefaultBottomSheetComponent
    @AssistedInject
    constructor(
        private val listMarkersComponentFactory: ListMarkersComponent.Factory,
        private val detailMarkerComponentFactory: DetailMarkerComponent.Factory,
        @Assisted("markersFlow") private val markersFlow: Flow<List<GeoMarker>>,
        @Assisted("selectedMarkerFlow") private val selectedMarkerFlow: Flow<GeoMarker?>,
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
                            markersFlow = markersFlow,
                            onMarkerSelected = { markerId ->
                                navigateToDetail(markerId)
                            },
                            component = componentContext,
                        )
                    BottomSheetComponent.Child.ListMarkers(component)
                }

                is Config.DetailMarker -> {
                    val component =
                        detailMarkerComponentFactory.create(
                            markerState = selectedMarkerFlow,
                            component = componentContext,
                        )
                    BottomSheetComponent.Child.DetailMarker(component)
                }
            }

        override fun navigateToList() {
            navigation.popToRoot()
        }

        override fun navigateToDetail(markerId: String) {
            navigation.push(Config.DetailMarker(markerId))
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

            @Serializable
            data class DetailMarker(
                val markerId: String,
            ) : Config
        }

        @AssistedFactory
        interface Factory {
            fun create(
                @Assisted markersFlow: Flow<List<GeoMarker>>,
                @Assisted selectedMarkerFlow: Flow<GeoMarker?>,
                @Assisted component: ComponentContext,
            ): DefaultBottomSheetComponent
        }
    }