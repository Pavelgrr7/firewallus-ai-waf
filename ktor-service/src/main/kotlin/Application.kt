package com.pavelryzh

import com.pavelryzh.plugins.*
import com.pavelryzh.plugins.di.configureDI
import io.ktor.server.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureHTTP()
    configureKafka()
    configureDI()
    configureRouting()
}
