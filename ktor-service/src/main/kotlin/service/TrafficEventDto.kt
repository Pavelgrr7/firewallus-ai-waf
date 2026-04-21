package com.pavelryzh.service

import kotlinx.serialization.Serializable

@Serializable
data class TrafficEventDto(val ip: String, val method: HttpMethod, val uri: String)

enum class HttpMethod {
    GET, POST, PUT, PATCH, DELETE, OPTIONS, UNKNOWN, HEAD, TRACE, CONNECT
}

fun parseMethod(s: String): HttpMethod {
    return runCatching { HttpMethod.valueOf(s.uppercase()) }
        .getOrDefault(HttpMethod.UNKNOWN)
}