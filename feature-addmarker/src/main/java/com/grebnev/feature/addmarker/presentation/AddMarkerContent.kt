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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.grebnev.core.map.extensions.calculateNewPosition
import com.grebnev.core.map.presentation.MapContent
import com.grebnev.core.map.presentation.MapStore
import com.grebnev.core.permissions.PermissionRequired
import com.grebnev.feature.addmarker.R
import com.grebnev.feature.imagepicker.presentation.ImagePickerContent

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AddMarkerContent(
    component: AddMarkerComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.model.subscribeAsState()
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    val context = LocalContext.current

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

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    BottomSheetScaffold(
        modifier = modifier.fillMaxSize(),
        scaffoldState = sheetState,
        topBar = {
            TopBar(
                titleScreen = stringResource(R.string.adding_new_marker),
                onBackClick = { component.onIntent(AddMarkerStore.Intent.BackClicked) },
            )
        },
        content = { padding ->
            Column(
                modifier =
                    modifier
                        .padding(
                            top = padding.calculateTopPadding(),
                            bottom = 10.dp,
                            start = 5.dp,
                            end = 5.dp,
                        ).fillMaxSize()
                        .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                OutlinedTextField(
                    value = state.title,
                    onValueChange = { title: String ->
                        component.onIntent(AddMarkerStore.Intent.TitleChanged(title))
                    },
                    label = { Text(stringResource(R.string.marker_name)) },
                    isError =
                        state.validationErrors.contains(
                            AddMarkerStore.State.ValidationError.TITLE_EMPTY,
                        ),
                    modifier = Modifier.fillMaxWidth(),
                )

                if (state.validationErrors.contains(AddMarkerStore.State.ValidationError.TITLE_EMPTY)) {
                    Text(
                        text = stringResource(R.string.name_required),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }

                OutlinedTextField(
                    value = state.description,
                    onValueChange = { description: String ->
                        component.onIntent(AddMarkerStore.Intent.DescriptionChanged(description))
                    },
                    label = { Text(stringResource(R.string.marker_description)) },
                    isError =
                        state.validationErrors.contains(
                            AddMarkerStore.State.ValidationError.DESCRIPTION_TOO_LONG,
                        ),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp),
                    maxLines = 4,
                )

                if (state.validationErrors.contains(
                        AddMarkerStore.State.ValidationError.DESCRIPTION_TOO_LONG,
                    )
                ) {
                    Text(
                        text = stringResource(R.string.description_too_long),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }

                Button(
                    onClick = {
                        component.onIntent(
                            AddMarkerStore.Intent.AddImagesClicked(state.selectedImages),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(imageVector = Icons.Default.Photo, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Images")
                }

                if (state.selectedImages.isNotEmpty()) {
                    LazyRow {
                        items(state.selectedImages) { imageUri ->
                            ImageThumbnailItem(
                                imageUri = imageUri,
                                onRemoveClick = {
                                    component.onIntent(AddMarkerStore.Intent.RemoveImage(imageUri))
                                },
                            )
                        }
                    }
                }

                Text(
                    text = stringResource(R.string.location),
                    style = MaterialTheme.typography.titleLarge,
                )

                MapContent(
                    component = component.mapComponent,
                    modifier =
                        Modifier
                            .height(300.dp)
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
            Box(
                modifier =
                    Modifier
                        .height(screenHeight / 2)
                        .fillMaxWidth(),
            ) {
                PermissionRequired(
                    context = context,
                    permission =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Manifest.permission.READ_MEDIA_IMAGES
                        } else {
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        },
                    permissionDescription =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            stringResource(com.grebnev.feature.imagepicker.R.string.media_images_access)
                        } else {
                            stringResource(com.grebnev.feature.imagepicker.R.string.external_storage_access)
                        },
                ) { permissionState ->
                    if (permissionState.status.isGranted) {
                        ImagePickerContent(
                            component = component.imagePickerComponent,
                        )
                    } else {
                        component.onIntent(AddMarkerStore.Intent.CancelImagesSelection)
                    }
                }
            }
        },
    )
}

@Composable
private fun ImageThumbnailItem(
    imageUri: Uri,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .padding(8.dp)
                .size(96.dp),
    ) {
        AsyncImage(
            model = imageUri,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    titleScreen: String,
    onBackClick: () -> Unit,
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
        title = { Text(text = titleScreen) },
        navigationIcon = {
            IconButton(onClick = { onBackClick() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                )
            }
        },
    )
}