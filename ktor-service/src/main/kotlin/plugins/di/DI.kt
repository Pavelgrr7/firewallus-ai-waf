package com.pavelryzh.plugins.di

import com.pavelryzh.core.WafRuleEngine
import com.pavelryzh.kafka.KafkaTrafficProducer
import com.pavelryzh.service.RedisWafClient
import com.pavelryzh.service.TrafficService
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.slf4j.LoggerFactory

fun Application.configureDI() {

    val lifecycleLogger = LoggerFactory.getLogger("Lifecycle")

    val redisUri = environment.config
        .propertyOrNull("ktor.redis.url")?.getString()
        ?: "redis://redis:6379"

    val bootstrapServers = environment.config
        .propertyOrNull("ktor.kafka.bootstrapServers")?.getString()
        ?: "kafka:29092"

    val appModule = module {
        single { WafRuleEngine() } onCloseWith lifecycleLogger
        single { RedisWafClient(redisUri) } onCloseWith lifecycleLogger
        single { KafkaTrafficProducer(bootstrapServers) } onCloseWith lifecycleLogger
        single { TrafficService(get(), get(), get()) } onCloseWith lifecycleLogger
    }

    install(Koin) {
        slf4jLogger()
        modules(appModule)
    }
}

