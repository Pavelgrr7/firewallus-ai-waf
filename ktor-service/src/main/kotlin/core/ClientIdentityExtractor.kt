package com.pavelryzh.core

import com.pavelryzh.model.ClientIdentity
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.origin
import java.security.MessageDigest

object IdentityExtractor {

    fun extract(call: ApplicationCall): ClientIdentity {
        val ip = call.request.origin.remoteHost

        val userAgent = call.request.headers["User-Agent"] ?: "unknown"
        val acceptLang = call.request.headers["Accept-Language"] ?: "unknown"
        val rawFingerprint = "$ip|$userAgent|$acceptLang"
        val fingerprintHash = md5(rawFingerprint)

        val authHeader = call.request.headers["Authorization"]
        val jwtHash = if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val token = authHeader.removePrefix("Bearer ")
            md5(token)
        } else {
            null
        }

        return ClientIdentity(ip, fingerprintHash, jwtHash)
    }

    // MD5 хэширование
    private fun md5(input: String): String {
        val bytes = MessageDigest.getInstance("MD5").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}