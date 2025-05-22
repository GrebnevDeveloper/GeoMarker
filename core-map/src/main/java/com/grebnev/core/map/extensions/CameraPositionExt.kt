package com.grebnev.core.map.extensions

import com.yandex.mapkit.map.CameraPosition
import kotlin.math.abs

fun CameraPosition.hasSignificantDifferenceFrom(other: CameraPosition): Boolean =
    abs(this.target.latitude - other.target.latitude) > 0.0001 ||
        abs(this.target.longitude - other.target.longitude) > 0.0001 ||
        abs(this.zoom - other.zoom) > 0.1f