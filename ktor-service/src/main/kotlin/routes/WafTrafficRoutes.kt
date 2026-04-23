package com.pavelryzh.routes

import com.pavelryzh.service.TrafficService
import io.ktor.server.routing.*

fun Route.wafTrafficRoutes(trafficService: TrafficService) {
    route("/{...}") {
        handle {
            trafficService.handleRequest(call)
        }
    }
}