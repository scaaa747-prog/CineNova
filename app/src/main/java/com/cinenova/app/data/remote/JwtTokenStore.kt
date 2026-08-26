package com.cinenova.app.data.remote

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Holds the bearer token obtained from the bootstrap exchange.
 *
 * Upstream returns an `x-user` response header containing
 * `{"token":"<jwt>"}`; [ResponseTokenCaptureInterceptor] stores it here and
 * [BearerAuthInterceptor] replays it as `Authorization: Bearer <token>`.
 *
 * Token issuance/signing itself lives behind [RequestAuthProvider] — inject
 * your authorized provider via [com.cinenova.app.di.ServiceLocator].
 */
object JwtTokenStore {

    private val _token = MutableStateFlow<String?>(null)

    /** Current JWT, or null before bootstrap. */
    val token: StateFlow<String?> = _token.asStateFlow()

    fun update(token: String?) {
        _token.value = token?.takeIf { it.isNotBlank() }
    }

    fun current(): String? = _token.value

    fun clear() {
        _token.value = null
    }
}
