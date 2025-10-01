package com.example.kotlin_traverse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.kotlin_traverse.core.TraverseAppContainer
import com.example.kotlin_traverse.ui.navigation.TraverseApp

class MainActivity : ComponentActivity() {

    private val appContainer: TraverseAppContainer by lazy {
        TraverseAppContainer(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TraverseApp(appContainer)
        }
    }
}