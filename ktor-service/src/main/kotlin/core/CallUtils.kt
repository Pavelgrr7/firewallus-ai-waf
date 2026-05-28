package com.pavelryzh.core

import com.pavelryzh.service.dto.TrafficEventDto
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.origin
import io.ktor.server.request.httpMethod
import io.ktor.server.request.uri

// Лимит для WAF: 16 КБ
const val WAF_PAYLOAD_LIMIT_BYTES = 16 * 1024

suspend fun extractTrafficLog(call: ApplicationCall, cachedBody: String? = null): TrafficEventDto {
    val headersMap = call.request.headers.entries()
        .associate { it.key.lowercase() to it.value.joinToString(",") }

    return TrafficEventDto(
        ip = call.request.origin.remoteHost,
        method = call.request.httpMethod.value,
        uri = call.request.uri,
        headers = headersMap,
        bodySnippet = cachedBody,
        bodyTruncated = cachedBody != null && cachedBody.length > WAF_PAYLOAD_LIMIT_BYTES
    )
}