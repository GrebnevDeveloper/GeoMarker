package com.grebnev.feature.detailsmarker.domain

import com.grebnev.core.common.wrappers.Result
import com.grebnev.core.database.repository.marker.GeoMarkerRepository
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
                .map { result ->
                    when (result) {
                        is Result.Success -> {
                            val marker = result.data
                            result.copy(
                                data = marker.copy(imagesUri = filterValidImagesUri(marker.imagesUri)),
                            )
                        }
                        else -> result
                    }
                }

        private suspend fun filterValidImagesUri(imagesUri: List<String>): List<String> =
            imagesUri.filter { imageUri ->
                galleryRepository.isUriValid(imageUri)
            }
    }