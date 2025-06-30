package com.grebnev.geomarker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.arkivanov.decompose.defaultComponentContext
import com.grebnev.geomarker.navigation.DefaultRootComponent
import com.grebnev.geomarker.navigation.RootContent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var rootComponentFactory: DefaultRootComponent.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val rootComponent = rootComponentFactory.create(defaultComponentContext())
        enableEdgeToEdge()
        setContent {
            RootContent(rootComponent)
        }
    }
}