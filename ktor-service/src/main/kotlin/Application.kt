package com.pavelryzh

import com.pavelryzh.plugins.configureKafka
import com.pavelryzh.plugins.configureSerialization
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureFrameworks()
    configureDatabases()
    configureSerialization()
//    configureSecurity()
    configureHTTP()
    configureRouting()
    configureKafka()
}
