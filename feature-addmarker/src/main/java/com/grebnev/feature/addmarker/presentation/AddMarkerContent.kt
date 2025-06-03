package com.grebnev.feature.addmarker.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.grebnev.core.map.presentation.MapContent
import com.grebnev.feature.addmarker.R

@Composable
fun AddMarkerContent(
    component: AddMarkerComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.model.subscribeAsState()
    val focusManager = LocalFocusManager.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopBar(
                titleScreen = stringResource(R.string.adding_new_marker),
                onBackClick = { component.onIntent(AddMarkerStore.Intent.BackClicked) },
            )
        },
    ) { padding ->
        Column(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = { title: String ->
                    component.onIntent(AddMarkerStore.Intent.TitleChanged(title))
                },
                label = { Text(stringResource(R.string.marker_name)) },
                isError = state.validationErrors.contains(AddMarkerStore.State.ValidationError.TITLE_EMPTY),
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

            if (state.validationErrors.contains(AddMarkerStore.State.ValidationError.DESCRIPTION_TOO_LONG)) {
                Text(
                    text = stringResource(R.string.description_too_long),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                )
            }

            MapContent(
                component = component.mapComponent,
                modifier = Modifier.height(300.dp),
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
                containerColor = Color.Transparent,
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