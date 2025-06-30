package com.grebnev.feature.geomarker.domain

import com.grebnev.core.common.wrappers.Result
import com.grebnev.core.database.repository.marker.GeoMarkerRepository
import com.grebnev.core.database.repository.position.LastPositionRepository
import com.grebnev.core.domain.entity.GeoMarker
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class GetNearestGeoMarkersUseCase
    @Inject
    constructor(
        private val geoMarkerRepository: GeoMarkerRepository,
        private val lastPositionRepository: LastPositionRepository,
    ) {
        @OptIn(FlowPreview::class)
        operator fun invoke(): Flow<Result<List<GeoMarker>>> {
            val positionFlow =
                lastPositionRepository
                    .getLastPositionFlow()
                    .distinctUntilChanged { old, new ->
                        old == null &&
                            new == null ||
                            old != null &&
                            new != null &&
                            calculateDistance(
                                old.latitude,
                                old.longitude,
                                new.latitude,
                                new.longitude,
                            ) < DISTANCE_THRESHOLD_IN_KM
                    }.debounce(DEBOUNCE_TIMEOUT_MILLISECONDS)

            val markersFlow =
                geoMarkerRepository
                    .getGeoMarkers()
                    .distinctUntilChanged()

            return combine(
                positionFlow,
                markersFlow,
            ) { lastPosition, markersResult ->
                when (markersResult) {
                    is Result.Success -> {
                        val sortedMarkers =
                            if (lastPosition != null) {
                                markersResult.data.sortedBy { marker ->
                                    calculateDistance(
                                        latPoint1 = lastPosition.latitude,
                                        lonPoint1 = lastPosition.longitude,
                                        latPoint2 = marker.latitude,
                                        lonPoint2 = marker.longitude,
                                    )
                                }
                            } else {
                                markersResult.data
                            }
                        Result.success(sortedMarkers)
                    }
                    else -> markersResult
                }
            }.distinctUntilChanged()
        }

        private fun calculateDistance(
            latPoint1: Double,
            lonPoint1: Double,
            latPoint2: Double,
            lonPoint2: Double,
        ): Double {
            val dLat = Math.toRadians(latPoint2 - latPoint1)
            val dLon = Math.toRadians(lonPoint2 - lonPoint1)
            val a =
                sin(dLat / 2).pow(2) +
                    cos(Math.toRadians(latPoint1)) * cos(Math.toRadians(latPoint2)) *
                    sin(dLon / 2).pow(2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return EARTH_RADIUS_IN_KM * c
        }

        companion object {
            private const val EARTH_RADIUS_IN_KM = 6371.0
            private const val DISTANCE_THRESHOLD_IN_KM = 0.1
            private const val DEBOUNCE_TIMEOUT_MILLISECONDS = 300L
        }
    }