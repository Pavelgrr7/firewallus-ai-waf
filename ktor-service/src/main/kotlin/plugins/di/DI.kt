package com.pavelryzh.plugins.di

import com.pavelryzh.core.WafRuleEngine
import com.pavelryzh.kafka.KafkaTrafficProducer
import com.pavelryzh.service.KtorHttpClient
import com.pavelryzh.service.ProxyHttpClient
import com.pavelryzh.service.RedisWafClient
import com.pavelryzh.service.TrafficService
import com.pavelryzh.service.WafConfigManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import kotlinx.serialization.json.Json
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.dsl.onClose
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
        single { HttpClient(CIO) {

            expectSuccess = false

            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                    }
                )
            }
            install(HttpRequestRetry) {
                wafRetryPolicy()
            }
        }} onClose { it?.close() }
        single { WafConfigManager(get() ) } onCloseWith lifecycleLogger
        single { KtorHttpClient(get()) } onCloseWith lifecycleLogger bind ProxyHttpClient::class
        single { TrafficService(get(), get(), get(), get(), get()) } onCloseWith lifecycleLogger
    }

    install(Koin) {
        slf4jLogger()
        modules(appModule)
    }
}

