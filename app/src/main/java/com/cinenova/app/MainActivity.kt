package com.cinenova.app

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.cinenova.app.data.AppStore
import com.cinenova.app.di.ServiceLocator
import com.cinenova.app.navigation.CineNovaApp
import com.cinenova.app.ui.theme.CineNovaTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppStore.init(this)
        ServiceLocator.catalogRepository.initCache(this)

        // Preload / prewarm catalog feed in background
        lifecycleScope.launch(Dispatchers.IO) {
            ServiceLocator.catalogRepository.bootstrap(forceRefresh = false)
        }

        val imageLoader = ImageLoader.Builder(this)
            .allowHardware(true)
            .crossfade(false)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCachePolicy(CachePolicy.ENABLED)
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(200L * 1024 * 1024)
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
