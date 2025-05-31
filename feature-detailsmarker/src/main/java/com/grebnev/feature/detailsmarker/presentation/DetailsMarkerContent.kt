package com.grebnev.feature.detailsmarker.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.grebnev.feature.detailsmarker.R

@Composable
fun DetailsMarkerContent(
    component: DetailsMarkerComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.model.subscribeAsState()
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
        ) {
            IconButton(onClick = { component.onIntent(DetailsMarkerStore.Intent.BackClicked) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                )
            }
            Text(
                text = state.marker?.title ?: "",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(start = 16.dp),
            )
        }

        Text(
            text = stringResource(R.string.description),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Text(
            text = state.marker?.description ?: "",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}