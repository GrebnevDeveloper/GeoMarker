package com.grebnev.core.gallery

import javax.inject.Inject

class GetImagesUriUseCase
    @Inject
    constructor(
        private val repository: GalleryRepository,
    ) {
        suspend operator fun invoke() = repository.getGalleryImagesUri()
    }