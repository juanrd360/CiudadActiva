package com.example.ciudadactiva

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ciudadactiva.navigation.CiudadActivaNavGraph
import com.example.ciudadactiva.ui.theme.CiudadActivaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CiudadActivaTheme {
                CiudadActivaNavGraph()
            }
        }
    }
}