package com.grebnev.core.database.database.converter

import androidx.room.TypeConverter
import com.yandex.mapkit.geometry.Point

object PositionConverter {
    private const val SEPARATOR = ";"

    @TypeConverter
    fun fromString(value: String): Point? =
        value.split(SEPARATOR).takeIf { it.size == 2 }?.let {
            Point(
                it[0].toDouble(),
                it[1].toDouble(),
            )
        }

    @TypeConverter
    fun toString(coordinates: Point): String =
        coordinates.let {
            "${it.latitude}$SEPARATOR${it.longitude}"
        }
}