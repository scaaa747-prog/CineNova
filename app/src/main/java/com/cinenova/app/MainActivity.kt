package com.cinenova.app

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.cinenova.app.data.AppStore
import com.cinenova.app.navigation.CineNovaApp
import com.cinenova.app.ui.theme.CineNovaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Low-data & memory-efficient ImageLoader configuration:
        // Uses RGB_565 (50% memory / data savings) + persistent disk cache
        val imageLoader = ImageLoader.Builder(this)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .allowRgb565(true)
            .crossfade(true)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.20)
                    .build()
            }
            .diskCachePolicy(CachePolicy.ENABLED)
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(150L * 1024 * 1024) // 150MB disk cache
                    .build()
            }
            .respectCacheHeaders(false)
            .build()
        Coil.setImageLoader(imageLoader)

        enableEdgeToEdge()
        setContent {
            CineNovaTheme(themeMode = AppStore.themeMode) {
                CineNovaApp()
            }
        }
    }
}
