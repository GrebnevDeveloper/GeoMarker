package com.grebnev.feature.geomarker.domain

import com.grebnev.core.database.repository.marker.GeoMarkerRepository
import com.grebnev.core.database.repository.position.LastPositionRepository
import kotlinx.coroutines.flow.combine
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
        operator fun invoke() =
            combine(
                lastPositionRepository.getLastPositionFlow(),
                geoMarkerRepository.getGeoMarkers(),
            ) { lastPosition, markers ->
                if (lastPosition != null) {
                    markers.sortedBy { marker ->
                        calculateDistance(
                            latPosition = lastPosition.latitude,
                            lonPosition = lastPosition.longitude,
                            latMarker = marker.latitude,
                            lonMarker = marker.longitude,
                        )
                    }
                } else {
                    markers
                }
            }.distinctUntilChanged { old, new ->
                old.size == new.size &&
                    old.firstOrNull()?.id == new.firstOrNull()?.id
            }

        private fun calculateDistance(
            latPosition: Double,
            lonPosition: Double,
            latMarker: Double,
            lonMarker: Double,
        ): Double {
            val dLat = Math.toRadians(latMarker - latPosition)
            val dLon = Math.toRadians(lonMarker - lonPosition)
            val a =
                sin(dLat / 2).pow(2) +
                    cos(Math.toRadians(latPosition)) * cos(Math.toRadians(latMarker)) *
                    sin(dLon / 2).pow(2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return EARTH_RADIUS_IN_KM * c
        }

        companion object {
            private const val EARTH_RADIUS_IN_KM = 6371.0
        }
    }