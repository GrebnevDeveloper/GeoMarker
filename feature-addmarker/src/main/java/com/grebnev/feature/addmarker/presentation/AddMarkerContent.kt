@file:OptIn(ExperimentalPermissionsApi::class)

package com.grebnev.feature.addmarker.presentation

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Scale
import coil3.size.Size
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.grebnev.core.map.extensions.calculateNewPosition
import com.grebnev.core.map.presentation.MapContent
import com.grebnev.core.map.presentation.MapStore
import com.grebnev.core.permissions.PermissionConstants
import com.grebnev.core.permissions.multiple.MultiplePermissionsRequest
import com.grebnev.feature.addmarker.R
import com.grebnev.feature.imagepicker.presentation.ImagePickerComponent
import com.grebnev.feature.imagepicker.presentation.ImagePickerContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMarkerContent(
    component: AddMarkerComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.model.subscribeAsState()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    val sheetState =
        rememberBottomSheetScaffoldState(
            bottomSheetState =
                rememberStandardBottomSheetState(
                    initialValue = SheetValue.Hidden,
                    skipHiddenState = false,
                ),
        )

    LaunchedEffect(state.showImagePicker) {
        if (state.showImagePicker) {
            sheetState.bottomSheetState.expand()
        } else {
            sheetState.bottomSheetState.hide()
        }
    }

    MultiplePermissionsRequest(
        permissions = PermissionConstants.requiredPermissions(),
    ) { permissionsState ->
        BottomSheetScaffold(
            modifier = modifier.fillMaxSize(),
            scaffoldState = sheetState,
            topBar = {
                TopBar(
                    titleScreen =
                        when (state.editorMode) {
                            EditorMode.ADD_MARKER -> stringResource(R.string.adding_new_marker)
                            EditorMode.EDIT_MARKER -> stringResource(R.string.edit_marker, state.title)
                        },
                    onBackClick = { component.onIntent(AddMarkerStore.Intent.BackClicked) },
                    onDeleteClick =
                        if (state.editorMode == EditorMode.EDIT_MARKER) {
                            { component.onIntent(AddMarkerStore.Intent.DeleteClicked) }
                        } else {
                            null
                        },
                )
            },
            content = { padding ->
                Column(
                    modifier =
                        modifier
                            .padding(
                                top = padding.calculateTopPadding(),
                                bottom = padding.calculateBottomPadding(),
                                start = 5.dp,
                                end = 5.dp,
                            ).fillMaxSize()
                            .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    TitleInputSection(
                        title = state.title,
                        validationError =
                            state.validationErrors.contains(
                                AddMarkerStore.State.ValidationError.TITLE_EMPTY,
                            ),
                        onTitleChanged = { title ->
                            component.onIntent(AddMarkerStore.Intent.TitleChanged(title))
                        },
                    )

                    DescriptionInputSection(
                        description = state.description,
                        validationError =
                            state.validationErrors.contains(
                                AddMarkerStore.State.ValidationError.DESCRIPTION_TOO_LONG,
                            ),
                        onDescriptionChanged = { description ->
                            component.onIntent(AddMarkerStore.Intent.DescriptionChanged(description))
                        },
                    )

                    ImagesSection(
                        selectedImages = state.selectedImages,
                        permissionsState = permissionsState,
                        onAddImagesClick = {
                            focusManager.clearFocus()
                            component.onIntent(AddMarkerStore.Intent.AddImagesClicked(state.selectedImages))
                        },
                        onRemoveImage = { imageUri ->
                            component.onIntent(AddMarkerStore.Intent.RemoveImage(imageUri))
                        },
                    )

                    Card(
                        modifier = modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.location),
                                style = MaterialTheme.typography.titleLarge,
                            )

                            val locationPermissionState =
                                permissionsState.find {
                                    it.permission == Manifest.permission.ACCESS_FINE_LOCATION
                                } ?: rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

                            MapContent(
                                component = component.mapComponent,
                                locationPermissionState = locationPermissionState,
                                modifier =
                                    Modifier
                                        .height(300.dp)
                                        .clip(MaterialTheme.shapes.small)
                                        .pointerInput(Unit) {
                                            detectTransformGestures(
                                                onGesture = { centroid, pan, zoom, rotation ->
                                                    component.mapComponent.onIntent(
                                                        MapStore.Intent.UpdateCameraPosition(
                                                            state.location.calculateNewPosition(
                                                                panOffset = pan,
                                                                zoomChange = zoom - 1f,
                                                            ),
                                                        ),
                                                    )
                                                },
                                            )
                                        },
                                showPositionMarker = true,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        modifier = modifier.fillMaxWidth(),
                        onClick = {
                            focusManager.clearFocus()
                            component.onIntent(AddMarkerStore.Intent.SubmitClicked)
                        },
                        enabled = state.isValid,
                    ) {
                        Text(stringResource(R.string.save))
                    }
                }
            },
            sheetContent = {
                ImagePickerSheetContent(component = component.imagePickerComponent)
            },
        )
    }
}

@Composable
private fun TitleInputSection(
    title: String,
    validationError: Boolean,
    onTitleChanged: (String) -> Unit,
) {
    OutlinedTextField(
        value = title,
        onValueChange = onTitleChanged,
        label = { Text(stringResource(R.string.marker_name)) },
        isError = validationError,
        modifier = Modifier.fillMaxWidth(),
    )

    if (validationError) {
        Text(
            text = stringResource(R.string.name_required),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
private fun DescriptionInputSection(
    description: String,
    validationError: Boolean,
    onDescriptionChanged: (String) -> Unit,
) {
    OutlinedTextField(
        value = description,
        onValueChange = onDescriptionChanged,
        label = { Text(stringResource(R.string.marker_description)) },
        isError = validationError,
        modifier = Modifier.fillMaxWidth(),
    )

    if (validationError) {
        Text(
            text = stringResource(R.string.description_too_long),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
private fun ImagesSection(
    selectedImages: List<Uri>,
    permissionsState: List<PermissionState>,
    onAddImagesClick: () -> Unit,
    onRemoveImage: (Uri) -> Unit,
    modifier: Modifier = Modifier,
) {
    val storagePermissionName =
        if (Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    val storagePermissionState =
        permissionsState.find { it.permission == storagePermissionName }
            ?: rememberPermissionState(storagePermissionName)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.images),
                style = MaterialTheme.typography.titleLarge,
            )

            Button(
                onClick = {
                    if (storagePermissionState.status.isGranted) {
                        onAddImagesClick()
                    } else {
                        storagePermissionState
                            .launchPermissionRequest()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(imageVector = Icons.Default.Photo, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.add_images))
            }

            if (selectedImages.isNotEmpty()) {
                LazyRow {
                    items(selectedImages) { imageUri ->
                        ImageThumbnailItem(
                            imageUri = imageUri,
                            onRemoveClick = { onRemoveImage(imageUri) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ImageThumbnailItem(
    imageUri: Uri,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Box(
        modifier =
            modifier
                .padding(8.dp)
                .size(96.dp),
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
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(8.dp),
                    ),
            placeholder = rememberAsyncImagePainter(com.grebnev.core.ui.R.drawable.ic_image_placeholder),
        )

        Box(
            modifier =
                Modifier
                    .size(25.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-6).dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f))
                    .clickable(onClick = onRemoveClick)
                    .padding(4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(12.dp),
            )
        }
    }
}

@Composable
private fun ImagePickerSheetContent(
    component: ImagePickerComponent,
    modifier: Modifier = Modifier,
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    Box(
        modifier =
            modifier
                .height(screenHeight / 2)
                .fillMaxWidth(),
    ) {
        ImagePickerContent(
            component = component,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    titleScreen: String,
    onBackClick: () -> Unit,
    onDeleteClick: (() -> Unit)? = null,
) {
    CenterAlignedTopAppBar(
        colors =
            TopAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                navigationIconContentColor = MaterialTheme.colorScheme.primary,
                actionIconContentColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.primary,
                scrolledContainerColor = MaterialTheme.colorScheme.background,
            ),
        title = {
            Text(
                text = titleScreen,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(end = 6.dp),
            )
        },
        navigationIcon = {
            IconButton(onClick = { onBackClick() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                )
            }
        },
        actions = {
            onDeleteClick?.let { deleteClick ->
                IconButton(onClick = { deleteClick() }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                    )
                }
            }
        },
    )
}