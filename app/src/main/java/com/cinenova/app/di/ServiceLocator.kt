package com.cinenova.app.di

import com.cinenova.app.data.remote.CatalogRepository
import com.cinenova.app.data.remote.MovieBoxApi
import com.cinenova.app.data.remote.MovieBoxClientFactory
import com.cinenova.app.data.remote.MovieBoxCatalogRepository
import com.cinenova.app.data.remote.MovieBoxRequestAuthProvider
import com.cinenova.app.data.remote.RequestAuthProvider

/**
 * Minimal service locator configured with the MovieBoxRequestAuthProvider.
 */
object ServiceLocator {

    val authProvider: RequestAuthProvider = MovieBoxRequestAuthProvider()

    val api: MovieBoxApi by lazy { MovieBoxClientFactory.create(authProvider) }

    val catalogRepository: CatalogRepository by lazy {
        MovieBoxCatalogRepository(api)
    }
}
