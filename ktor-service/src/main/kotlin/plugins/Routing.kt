package com.pavelryzh.plugins

import com.pavelryzh.routes.wafTrafficRoutes
import com.pavelryzh.kafka.KafkaTrafficProducer
import com.pavelryzh.service.TrafficService
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.httpMethod
import io.ktor.server.request.uri
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val trafficService by inject<TrafficService>()

    routing {
        wafTrafficRoutes(trafficService)
    }
}