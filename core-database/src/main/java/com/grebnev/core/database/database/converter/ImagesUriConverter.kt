package com.grebnev.core.database.database.converter

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json

class ImagesUriConverter {
    @TypeConverter
    fun fromStringList(value: List<String>): String = Json.encodeToString(value)

    @TypeConverter
    fun toStringList(value: String): List<String> = Json.decodeFromString(value)
}