package com.grebnev.core.gallery

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GalleryRepositoryImpl
    @Inject
    constructor(
        private val context: Context,
    ) : GalleryRepository {
        override suspend fun getGalleryImagesUri(): List<Uri> =
            withContext(Dispatchers.IO) {
                val imagesUri = mutableListOf<Uri>()
                val projection =
                    arrayOf(
                        MediaStore.Images.Media._ID,
                    )

                val selection =
                    "${MediaStore.Images.Media.MIME_TYPE} = ? OR " +
                        "${MediaStore.Images.Media.MIME_TYPE} = ? OR " +
                        "${MediaStore.Images.Media.MIME_TYPE} = ?"

                val selectionArgs =
                    arrayOf(
                        "image/jpeg",
                        "image/png",
                        "image/webp",
                    )

                val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

                context.contentResolver
                    .query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        projection,
                        selection,
                        selectionArgs,
                        sortOrder,
                    )?.use { cursor ->
                        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)

                        while (cursor.moveToNext()) {
                            val id = cursor.getLong(idColumn)
                            val uri =
                                ContentUris.withAppendedId(
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    id,
                                )

                            imagesUri.add(uri)
                        }
                    }
                imagesUri
            }

        override suspend fun takePhoto(): Uri? {
            TODO("Not yet implemented")
        }
    }