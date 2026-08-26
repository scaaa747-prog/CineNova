package com.cinenova.app.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MovieBoxRequestAuthProviderTest {

    private val provider = MovieBoxRequestAuthProvider(
        secretKey = "76iRl07s0xSN9jqmEWAt79EBJZulIQIsV64FZr2O",
        versionCode = 50020045,
        deviceId = "testdevice12345",
        gaid = "testgaid12345",
    )

    @Test
    fun testClientTokenGeneration() {
        val ts = 1787741984235L
        val token = provider.generateXClientToken(ts)
        assertTrue(token.startsWith("1787741984235,"))
        assertEquals(2, token.split(",").size)
    }

    @Test
    fun testSignatureGeneration() {
        val ts = 1787741984235L
        val url = "https://api6.aoneroom.com/wefeed-mobile-bff/tab-operating?page=1&tabId=0&version="
        val sig = provider.generateXTrSignature(
            method = "GET",
            accept = "application/json",
            contentType = "application/json",
            url = url,
            body = null,
            ts = ts,
        )
        assertTrue(sig.startsWith("1787741984235|2|"))
        assertNotNull(sig)
    }

    @Test
    fun testHeadersContainRequiredFields() {
        val headers = provider.headers(
            method = "GET",
            url = "https://api6.aoneroom.com/wefeed-mobile-bff/tab-operating?page=1&tabId=0&version=",
            requestBody = null,
        )
        assertNotNull(headers["User-Agent"])
        assertNotNull(headers["X-Client-Token"])
        assertNotNull(headers["x-tr-signature"])
        assertNotNull(headers["X-Client-Info"])
        assertEquals("0", headers["X-Client-Status"])
    }
}
