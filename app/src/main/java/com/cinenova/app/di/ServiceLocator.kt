package com.cinenova.app.di

import com.cinenova.app.data.remote.CatalogRepository
import com.cinenova.app.data.remote.MovieBoxApi
import com.cinenova.app.data.remote.MovieBoxClientFactory
import com.cinenova.app.data.remote.MovieBoxCatalogRepository
import com.cinenova.app.data.remote.NoOpRequestAuthProvider
import com.cinenova.app.data.remote.RequestAuthProvider

/**
 * Minimal service locator. Swap [authProvider] for the authorized signing
 * implementation when available — nothing else in the app changes.
 */
object ServiceLocator {

    /**
     * ┌──────────────────────────────────────────────────────────────┐
     * │ INJECTION POINT                                              │
     * │ Replace with the real RequestAuthProvider implementation.    │
     * └──────────────────────────────────────────────────────────────┘
     */
    val authProvider: RequestAuthProvider = NoOpRequestAuthProvider()

    val api: MovieBoxApi by lazy { MovieBoxClientFactory.create(authProvider) }

    val catalogRepository: CatalogRepository by lazy {
        MovieBoxCatalogRepository(api)
    }
}
