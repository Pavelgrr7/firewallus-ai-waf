package com.pavelryzh.routes

import com.pavelryzh.service.TrafficEventDto
import com.pavelryzh.service.TrafficService
import com.pavelryzh.service.parseMethod
import io.ktor.server.plugins.origin
import io.ktor.server.request.httpMethod
import io.ktor.server.request.uri
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.wafTrafficRoutes(trafficService: TrafficService) {
    route("/{...}") {
        handle {
            val ip = call.request.origin.remoteHost
            val method = parseMethod(call.request.httpMethod.value)
            val uri = call.request.uri

            trafficService.processTraffic(TrafficEventDto(ip, method, uri));

            call.respondText("Hello, World! Your request is being analyzed out-of-band.")
        }
    }
}