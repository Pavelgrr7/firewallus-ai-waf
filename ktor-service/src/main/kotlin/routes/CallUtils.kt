package com.pavelryzh.routes

import com.pavelryzh.service.TrafficEventDto
import com.pavelryzh.service.parseMethod
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.origin
import io.ktor.server.request.httpMethod
import io.ktor.server.request.uri

fun extractTrafficLog(call: ApplicationCall): TrafficEventDto {
    return TrafficEventDto(
        ip = call.request.origin.remoteHost,
        method = parseMethod(call.request.httpMethod.value),
        uri = call.request.uri
    )
}