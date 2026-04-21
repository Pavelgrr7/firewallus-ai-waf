package com.pavelryzh

import com.pavelryzh.plugins.configureDI
import com.pavelryzh.plugins.configureHTTP
import com.pavelryzh.plugins.configureKafka
import com.pavelryzh.plugins.configureSerialization
import com.pavelryzh.kafka.KafkaTrafficProducer
import com.pavelryzh.plugins.configureRouting
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.forwardedheaders.XForwardedHeaders
import org.koin.ktor.ext.get
import com.pavelryzh.plugins.configureDatabases

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    install(XForwardedHeaders)
    configureDatabases()
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