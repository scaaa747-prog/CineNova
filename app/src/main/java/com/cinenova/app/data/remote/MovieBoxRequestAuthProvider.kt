package com.cinenova.app.data.remote

import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Base64
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Production implementation of [RequestAuthProvider] that dynamically computes
 * cryptographic signatures (HMAC-MD5, X-Client-Token, X-Client-Info) required
 * by the upstream MovieBox v3 mobile BFF.
 */
class MovieBoxRequestAuthProvider(
    private val secretKey: String = SECRET_KEY_DEFAULT,
    private val versionCode: Int = VERSION_CODE_DEFAULT,
    private val deviceId: String = UUID.randomUUID().toString().replace("-", ""),
    private val gaid: String = UUID.randomUUID().toString(),
) : RequestAuthProvider {

    private val userAgent =
        "com.community.oneroom/$versionCode (Linux; U; Android 13; en_US; 22101316G; Build/TQ2A.230405.003; Cronet/135.0.7012.3)"

    private val clientInfoJson = """
        {"package_name":"com.community.oneroom","version_name":"3.0.03.0529.03","version_code":$versionCode,"os":"android","os_version":"13","install_ch":"ps","device_id":"$deviceId","install_store":"ps","gaid":"$gaid","brand":"Redmi","model":"22101316G","system_language":"en","net":"NETWORK_WIFI","region":"US","timezone":"America/New_York","sp_code":"40401","X-Play-Mode":"2"}
    """.trimIndent().replace("\n", "")

    override fun headers(method: String, url: String, requestBody: String?): Map<String, String> {
        val ts = System.currentTimeMillis()
        val accept = "application/json"
        val contentType = "application/json"

        val xClientToken = generateXClientToken(ts)
        val xTrSignature = generateXTrSignature(
            method = method,
            accept = accept,
            contentType = contentType,
            url = url,
            body = requestBody,
            ts = ts,
        )

        return mapOf(
            "User-Agent" to userAgent,
            "Accept" to accept,
            "Content-Type" to contentType,
            "Connection" to "keep-alive",
            "X-Client-Token" to xClientToken,
            "x-tr-signature" to xTrSignature,
            "X-Client-Info" to clientInfoJson,
            "X-Client-Status" to "0",
        )
    }

    private fun md5Hex(data: ByteArray): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(data)
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun generateXClientToken(ts: Long): String {
        val reversedTs = ts.toString().reversed()
        val hash = md5Hex(reversedTs.toByteArray(StandardCharsets.UTF_8))
        return "$ts,$hash"
    }

    private fun sortedQueryString(url: String): String {
        val uri = URI(url)
        val query = uri.rawQuery ?: return ""
        if (query.isBlank()) return ""

        val pairs = query.split("&")
            .mapNotNull { pair ->
                val idx = pair.indexOf("=")
                if (idx >= 0) {
                    val key = URLDecoder.decode(pair.substring(0, idx), "UTF-8")
                    val value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8")
                    key to value
                } else if (pair.isNotEmpty()) {
                    URLDecoder.decode(pair, "UTF-8") to ""
                } else {
                    null
                }
            }
            .sortedBy { it.first }

        return pairs.joinToString("&") { (k, v) ->
            "${URLEncoder.encode(k, "UTF-8")}=${URLEncoder.encode(v, "UTF-8")}"
        }
    }

    fun generateXTrSignature(
        method: String,
        accept: String,
        contentType: String,
        url: String,
        body: String?,
        ts: Long,
    ): String {
        val uri = URI(url)
        val path = uri.path ?: "/"
        val sortedQuery = sortedQueryString(url)
        val canonicalUrl = if (sortedQuery.isNotEmpty()) "$path?$sortedQuery" else path

        var bodyHash = ""
        var bodyLength = ""
        if (body != null) {
            val bodyBytes = body.toByteArray(StandardCharsets.UTF_8)
            val chunk = if (bodyBytes.size > 4096) bodyBytes.copyOfRange(0, 4096) else bodyBytes
            bodyHash = md5Hex(chunk)
            bodyLength = bodyBytes.size.toString()
        }

        val canonicalParts = listOf(
            method.uppercase(),
            accept,
            contentType,
            bodyLength,
            ts.toString(),
            bodyHash,
            canonicalUrl,
        )
        val canonicalString = canonicalParts.joinToString("\n")

        val secretBytes = Base64.getDecoder().decode(secretKey)
        val mac = Mac.getInstance("HmacMD5")
        mac.init(SecretKeySpec(secretBytes, "HmacMD5"))
        val signatureBytes = mac.doFinal(canonicalString.toByteArray(StandardCharsets.UTF_8))
        val base64Signature = Base64.getEncoder().encodeToString(signatureBytes)

        return "$ts|2|$base64Signature"
    }

    companion object {
        const val SECRET_KEY_DEFAULT = "76iRl07s0xSN9jqmEWAt79EBJZulIQIsV64FZr2O"
        const val VERSION_CODE_DEFAULT = 50020045
    }
}
