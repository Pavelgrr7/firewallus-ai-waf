package com.pavelryzh

import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureFrameworks()
    configureDatabases()
    configureSerialization()
    configureSecurity()
    configureHTTP()
    configureRouting()
    configureKafka()
}
