package com.grebnev.core.gallery

import android.net.Uri

interface GalleryRepository {
    suspend fun getGalleryImagesUri(): List<Uri>

    suspend fun takePhoto(): Uri?
}