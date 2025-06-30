package com.grebnev.core.database.repository.position

import com.yandex.mapkit.geometry.Point
import kotlinx.coroutines.flow.Flow

interface LastPositionRepository {
    suspend fun getLastPosition(): Point?

    fun getLastPositionFlow(): Flow<Point?>

    suspend fun updateLastPosition(point: Point)
}