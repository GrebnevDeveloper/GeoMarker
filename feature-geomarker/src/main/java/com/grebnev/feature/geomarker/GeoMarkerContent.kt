package com.grebnev.feature.geomarker

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddLocationAlt
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.grebnev.core.map.MapContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeoMarkerContent(component: GeoMarkerComponent) {
    val sheetState = rememberBottomSheetScaffoldState()

    BottomSheetScaffold(
        scaffoldState = sheetState,
        content = { paddingValues ->
            Box {
                MapContent()
                FloatingActionButton(
                    modifier =
                        Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 10.dp, bottom = 88.dp)
                            .size(70.dp),
                    onClick = {},
                    shape = CircleShape,
                ) {
                    Icon(
                        modifier = Modifier.size(35.dp),
                        imageVector = Icons.Rounded.AddLocationAlt,
                        contentDescription = null,
                    )
                }
            }
        },
        sheetContent = {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Места рядом", style = MaterialTheme.typography.headlineSmall)
                LazyColumn {
                    items(10) { index ->
                        Text("Место $index", modifier = Modifier.padding(8.dp))
                    }
                }
            }
        },
    )
}