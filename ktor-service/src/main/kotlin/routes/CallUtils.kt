package com.pavelryzh.routes

import com.pavelryzh.service.dto.TrafficEventDto
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.origin
import io.ktor.server.request.httpMethod
import io.ktor.server.request.uri

fun extractTrafficLog(call: ApplicationCall): TrafficEventDto {
    val headersMap = call.request.headers.entries()
        .associate { it.key.lowercase() to it.value.joinToString(",") }

    return TrafficEventDto(
        ip = call.request.origin.remoteHost,
        method = call.request.httpMethod.value,
        uri = call.request.uri,
        headers = headersMap
    )
}