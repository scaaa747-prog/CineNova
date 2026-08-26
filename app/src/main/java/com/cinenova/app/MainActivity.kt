package com.cinenova.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.cinenova.app.data.AppStore
import com.cinenova.app.navigation.CineNovaApp
import com.cinenova.app.ui.theme.CineNovaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CineNovaTheme(themeMode = AppStore.themeMode) {
                CineNovaApp()
            }
        }
    }
}
