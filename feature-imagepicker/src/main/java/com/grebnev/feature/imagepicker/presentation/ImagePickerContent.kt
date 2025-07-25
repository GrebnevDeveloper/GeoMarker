package com.grebnev.feature.imagepicker.presentation

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Scale
import coil3.size.Size
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.grebnev.core.permissions.single.PermissionRequest
import com.grebnev.core.ui.R

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ImagePickerContent(
    component: ImagePickerComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.model.subscribeAsState()
    val takePhotoLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { result ->
            component.onIntent(ImagePickerStore.Intent.PhotoTaken(result))
        }

    Column(
        modifier = modifier.padding(16.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            TextButton(
                onClick = { component.onIntent(ImagePickerStore.Intent.CancelClicked) },
            ) {
                Text(stringResource(com.grebnev.feature.imagepicker.R.string.cancel))
            }

            Text(
                text = stringResource(com.grebnev.feature.imagepicker.R.string.select_images),
                style = MaterialTheme.typography.titleMedium,
            )

            TextButton(
                onClick = { component.onIntent(ImagePickerStore.Intent.ConfirmClicked) },
                enabled = state.selectedImagesUri.isNotEmpty(),
            ) {
                Text(stringResource(com.grebnev.feature.imagepicker.R.string.confirm))
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.heightIn(max = 400.dp),
        ) {
            item {
                PermissionRequest(
                    permission = Manifest.permission.CAMERA,
                ) { permissionState, onPermissionCheck ->
                    CameraPlaceholderItem(
                        onClick = {
                            if (permissionState.status.isGranted) {
                                state.photoUri?.let {
                                    takePhotoLauncher.launch(it)
                                }
                            } else {
                                onPermissionCheck()
                            }
                        },
                    )
                }
            }

            items(items = state.availableImagesUri) { imageUri ->
                ImageThumbnailItem(
                    imageUri = imageUri,
                    isSelected = state.selectedImagesUri.contains(imageUri),
                    onClick = { component.onIntent(ImagePickerStore.Intent.ImageClicked(imageUri)) },
                )
            }
        }
    }
}

@Composable
private fun CameraPlaceholderItem(onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            Modifier
                .aspectRatio(1f)
                .padding(4.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                .clickable(onClick = onClick),
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
        )
    }
}

@Composable
private fun ImageThumbnailItem(
    imageUri: Uri,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val context = LocalContext.current

    Box(
        contentAlignment = Alignment.Center,
        modifier =
            Modifier
                .aspectRatio(1f)
                .padding(4.dp),
    ) {
        AsyncImage(
            model =
                ImageRequest
                    .Builder(context)
                    .data(imageUri)
                    .size(Size(200, 200))
                    .scale(Scale.FILL)
                    .crossfade(true)
                    .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onClick),
            placeholder =
                rememberAsyncImagePainter(R.drawable.ic_image_placeholder),
        )

        if (isSelected) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f)),
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                )
            }
        }
    }
}