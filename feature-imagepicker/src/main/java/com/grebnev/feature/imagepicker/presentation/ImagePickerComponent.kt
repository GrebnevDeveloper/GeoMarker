package com.grebnev.feature.imagepicker.presentation

import com.arkivanov.decompose.value.Value

interface ImagePickerComponent {
    val model: Value<ImagePickerStore.State>

    fun onIntent(intent: ImagePickerStore.Intent)
}