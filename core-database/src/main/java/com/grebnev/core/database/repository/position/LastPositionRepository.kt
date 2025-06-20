package com.grebnev.core.database.repository.position

import com.yandex.mapkit.geometry.Point

interface LastPositionRepository {
    suspend fun getLastPosition(): Point?

    suspend fun updateLastPosition(point: Point)
}