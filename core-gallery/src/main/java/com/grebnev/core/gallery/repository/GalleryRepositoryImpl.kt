package com.grebnev.core.gallery.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.grebnev.core.gallery.domain.repository.GalleryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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

        override suspend fun getPhotoUri(): Uri? =
            withContext(Dispatchers.IO) {
                try {
                    val photoFile =
                        createTempImageFile(context).also {
                            it.createNewFile()
                        }

                    val photoUri =
                        FileProvider.getUriForFile(
                            context,
                            "${context.packageName}${FILE_PROVIDER_NAME}",
                            photoFile,
                        )

                    photoUri
                } catch (e: Exception) {
                    null
                }
            }

        private fun createTempImageFile(context: Context): File {
            val timeStamp = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(Date())
            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            return File.createTempFile(
                "JPEG_${timeStamp}_",
                ".jpg",
                storageDir,
            )
        }

        override suspend fun isUriValid(imageUri: String): Boolean =
            withContext(Dispatchers.IO) {
                try {
                    val uri = imageUri.toUri()

                    if (uri.authority == MediaStore.AUTHORITY) {
                        return@withContext checkMediaStoreUri(uri)
                    }

                    if (uri.authority == "${context.packageName}${FILE_PROVIDER_NAME}") {
                        return@withContext checkFileProviderUri(uri)
                    }

                    return@withContext checkFileUri(uri)
                } catch (e: Exception) {
                    false
                }
            }

        private fun checkMediaStoreUri(uri: Uri): Boolean {
            val projection = arrayOf(MediaStore.Images.Media._ID)
            return context.contentResolver
                .query(
                    uri,
                    projection,
                    null,
                    null,
                    null,
                )?.use { cursor ->
                    cursor.moveToFirst()
                } == true
        }

        private fun checkFileProviderUri(uri: Uri): Boolean =
            try {
                context.contentResolver.openInputStream(uri)?.use {
                    true
                } == true
            } catch (e: FileNotFoundException) {
                false
            }

        private fun checkFileUri(uri: Uri): Boolean {
            return when (uri.scheme) {
                "file" -> File(uri.path ?: return false).exists()
                "content" -> {
                    context.contentResolver
                        .query(
                            uri,
                            null,
                            null,
                            null,
                            null,
                        )?.use { true } == true
                }
                else -> false
            }
        }

        companion object {
            private const val FILE_PROVIDER_NAME = ".fileprovider"
            private const val DATE_FORMAT = "yyyyMMdd_HHmmss"
        }
    }