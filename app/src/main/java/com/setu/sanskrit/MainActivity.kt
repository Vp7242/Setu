package com.setu.sanskrit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.setu.sanskrit.ui.navigation.SetuApp
import com.setu.sanskrit.ui.theme.SetuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as SetuApplication).container
        setContent { SetuTheme { SetuApp(container) } }
    }
}
