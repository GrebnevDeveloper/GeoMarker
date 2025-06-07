package com.grebnev.core.gallery.domain.usecase

import com.grebnev.core.gallery.domain.repository.GalleryRepository
import javax.inject.Inject

class GetImagesUriUseCase
    @Inject
    constructor(
        private val repository: GalleryRepository,
    ) {
        suspend operator fun invoke() = repository.getGalleryImagesUri()
    }