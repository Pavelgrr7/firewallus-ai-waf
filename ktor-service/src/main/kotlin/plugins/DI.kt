package com.pavelryzh.plugins

import com.pavelryzh.kafka.KafkaTrafficProducer
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger



fun Application.configureDI() {
    val bootstrapServers = environment.config
        .propertyOrNull("ktor.kafka.common.bootstrap.servers")?.getString()
        ?: "kafka:29092"
    val appModule = module {
        single { KafkaTrafficProducer(bootstrapServers) }
    }

    install(Koin) {
        slf4jLogger()
        modules(appModule)
    }
}