package com.grebnev.feature.detailsmarker.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Scale
import coil3.size.Size
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.grebnev.feature.detailsmarker.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsMarkerContent(
    component: DetailsMarkerComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.model.subscribeAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        val title = state.marker.title
        if (title.isNotEmpty()) {
            TitleSection(
                title = title,
                onClick = { component.onIntent(DetailsMarkerStore.Intent.BackClicked) },
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
        key(state.marker.hashCode()) {
            LazyColumn(
                modifier =
                    modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val imagesUri = state.marker.imagesUri
                if (imagesUri.isNotEmpty()) {
                    item {
                        ImagesSection(imagesUri)
                    }
                }

                val description = state.marker.description
                if (description.isNotEmpty()) {
                    item {
                        DescriptionSection(description)
                    }
                }
            }
        }
    }
}

@Composable
private fun TitleSection(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 16.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImagesSection(imagesUri: List<String>) {
    val context = LocalContext.current
    val carouselState = rememberCarouselState(itemCount = { imagesUri.size })

    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.place_images),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            HorizontalMultiBrowseCarousel(
                state = carouselState,
                modifier = Modifier.height(200.dp),
                preferredItemWidth = 250.dp,
                itemSpacing = 10.dp,
            ) { index ->
                AsyncImage(
                    model =
                        ImageRequest
                            .Builder(context)
                            .data(imagesUri[index].toUri())
                            .size(Size(500, 500))
                            .scale(Scale.FILL)
                            .crossfade(true)
                            .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp)),
                    placeholder = rememberVectorPainter(Icons.Outlined.Photo),
                )
            }
        }
    }
}

@Composable
private fun DescriptionSection(description: String) {
    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.description),
                style = MaterialTheme.typography.titleLarge,
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}