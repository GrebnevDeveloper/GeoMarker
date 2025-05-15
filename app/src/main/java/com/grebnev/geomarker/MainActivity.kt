package com.grebnev.geomarker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.grebnev.feature.geomarker.GeoMarkerScreen
import com.grebnev.geomarker.ui.theme.GeoMarkerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GeoMarkerTheme {
                GeoMarkerScreen()
            }
        }
    }
}