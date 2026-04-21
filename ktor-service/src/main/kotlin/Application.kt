package com.pavelryzh

import com.pavelryzh.kafka.KafkaTrafficProducer
import com.pavelryzh.plugins.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import org.koin.ktor.ext.get

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureHTTP()
    configureKafka()
    configureDI()
    configureRouting()

    monitor.subscribe(ApplicationStopped) {
        val kafkaProducer = get<KafkaTrafficProducer>()
        kafkaProducer.close()
        log.info("KafkaTrafficProducer closed successfully.")
    }
}