package com.cinenova.app.data.remote

/**
 * Injectable contract for request authentication/signing.
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ PLACEHOLDER — DO NOT IMPLEMENT CRYPTOGRAPHIC SIGNING HERE.             │
 * │                                                                        │
 * │ The authorized implementation (token bootstrapping, X-Client-Token,    │
 * │ x-tr-signature HMAC, X-Client-Info, User-Agent policy, etc.) is        │
 * │ supplied separately and plugged in via [ServiceLocator].               │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * Implementations receive the fully-resolved request coordinates and return
 * the headers to attach. Return an empty map for unsigned requests.
 */
interface RequestAuthProvider {
    /**
     * @param method HTTP method, e.g. "GET".
     * @param url Fully-qualified request URL (path + sorted query included).
     * @param requestBody Serialized body string, or null for bodyless requests.
     * @return Headers to merge into the outgoing request.
     */
    fun headers(method: String, url: String, requestBody: String?): Map<String, String>
}

/** Default no-op provider used until the real signing implementation is injected. */
class NoOpRequestAuthProvider : RequestAuthProvider {
    override fun headers(method: String, url: String, requestBody: String?): Map<String, String> =
        emptyMap()
}
