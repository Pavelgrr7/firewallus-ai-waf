package com.pavelryzh.plugins

import com.pavelryzh.kafka.KafkaTrafficProducer
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

val appModule = module {
    single { KafkaTrafficProducer("kafka:29092") }
}

fun Application.configureDI() {
    install(Koin) {
        slf4jLogger()
        modules(appModule)
    }
}