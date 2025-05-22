package com.grebnev.core.location.data

import android.Manifest
import android.content.Context
import android.os.Looper
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.grebnev.core.location.domain.entity.LocationStatus
import com.grebnev.core.location.domain.repository.LocationRepository
import com.yandex.mapkit.geometry.Point
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class LocationRepositoryImpl
    @Inject
    constructor(
        private val context: Context,
    ) : LocationRepository {
        private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        private val locationStatus = MutableStateFlow<LocationStatus>(LocationStatus.Initial)

        private val locationCallback =
            object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.lastLocation?.let { location ->
                        locationStatus.value =
                            LocationStatus.Available(
                                Point(location.latitude, location.longitude),
                            )
                    }
                }

                override fun onLocationAvailability(availability: LocationAvailability) {
                    if (!availability.isLocationAvailable) {
                        locationStatus.value = LocationStatus.Error("Location error")
                    }
                }
            }

        override val getCurrentLocation: StateFlow<LocationStatus> = locationStatus.asStateFlow()

        @RequiresPermission(
            allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION],
        )
        override fun startLocationUpdates(minInterval: Long) {
            val request =
                LocationRequest
                    .Builder(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        minInterval,
                    ).build()

            fusedLocationClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper(),
            )
            locationStatus.value = LocationStatus.Loading
        }

        override fun stopLocationUpdates() {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            locationStatus.value = LocationStatus.Initial
        }
    }