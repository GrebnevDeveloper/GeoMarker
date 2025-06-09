package com.grebnev.feature.detailsmarker.domain

import com.grebnev.core.database.repository.GeoMarkerRepository
import com.grebnev.core.gallery.domain.repository.GalleryRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetDetailsMarkerUseCase
    @Inject
    constructor(
        private val geoMarkerRepository: GeoMarkerRepository,
        private val galleryRepository: GalleryRepository,
    ) {
        operator fun invoke(markerId: Long) =
            geoMarkerRepository
                .getGeoMarkerById(markerId)
                .map { marker ->
                    marker.copy(imagesUri = filterValidImagesUri(marker.imagesUri))
                }

        private suspend fun filterValidImagesUri(imagesUri: List<String>): List<String> =
            imagesUri.filter { imageUri ->
                galleryRepository.isUriValid(imageUri)
            }
    }