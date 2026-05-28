package com.pavelryzh.service.dto

import kotlinx.serialization.Serializable

@Serializable
data class TrafficEventDto(
    val ip: String,
    val method: String,
    val uri: String,
    val headers: Map<String, String>,
    val bodySnippet: String? = null,
    val bodyTruncated: Boolean = false
) : KafkaEvent

enum class HttpMethod {
    GET, POST, PUT, PATCH, DELETE, OPTIONS, UNKNOWN, HEAD, TRACE, CONNECT
}

fun parseMethod(s: String): HttpMethod {
    return when {
        s.equals("GET", ignoreCase = true) -> HttpMethod.GET
        s.equals("POST", ignoreCase = true) -> HttpMethod.POST
        s.equals("PUT", ignoreCase = true) -> HttpMethod.PUT
        s.equals("PATCH", ignoreCase = true) -> HttpMethod.PATCH
        s.equals("DELETE", ignoreCase = true) -> HttpMethod.DELETE
        s.equals("OPTIONS", ignoreCase = true) -> HttpMethod.OPTIONS
        s.equals("HEAD", ignoreCase = true) -> HttpMethod.HEAD
        s.equals("TRACE", ignoreCase = true) -> HttpMethod.TRACE
        s.equals("CONNECT", ignoreCase = true) -> HttpMethod.CONNECT
        else -> HttpMethod.UNKNOWN
    }
}