package com.grebnev.core.gallery.domain.repository

import android.net.Uri

interface GalleryRepository {
    suspend fun getGalleryImagesUri(): List<Uri>

    suspend fun getPhotoUri(): Uri?
}