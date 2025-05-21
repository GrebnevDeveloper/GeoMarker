package com.grebnev.core.map

interface MapComponent {
    fun onStartUpdates()

    fun onStopUpdates()

    fun onMoveToMyLocation()

    fun onChangeZoom(delta: Float)
}