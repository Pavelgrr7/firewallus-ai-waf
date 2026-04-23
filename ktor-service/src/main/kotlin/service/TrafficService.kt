package com.pavelryzh.service

import com.pavelryzh.kafka.KafkaTrafficProducer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import com.pavelryzh.plugins.logger
import com.pavelryzh.routes.extractTrafficLog
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.origin
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull

class TrafficService(
    private val kafkaProducer: KafkaTrafficProducer,
    private val redisWafClient: RedisWafClient,
    ) : AutoCloseable {

    private val exceptionHandler = CoroutineExceptionHandler { _, ex ->
        logger.error("Failed to send traffic log to Kafka", ex)
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + exceptionHandler)

    private val logger = logger()

    suspend fun handleRequest(call: ApplicationCall) {
        val ip = call.request.origin.remoteHost

        // Fail-Open: если Redis недоступен, разрешаем трафик (безопаснее, чем Fail-Closed)
        val isBanned = runCatching { redisWafClient.isIpBanned(ip) }
            .onFailure { logger.error("Redis is down, allowing traffic (Fail-Open)", it) }
            .getOrDefault(false)

        if (isBanned) {
            call.respond(HttpStatusCode.Forbidden, "Access Denied by WAF")
            return
        }

        // todo статические правила (пока что в redis они всё равно никак не попадают
        // val rules = redis.getActiveRules()
        // if (violatesRules(call, rules)) { ... return }

        val trafficLog = extractTrafficLog(call)
        // корутина не блокирует основной поток запроса
        serviceScope.launch {
            kafkaProducer.send(trafficLog)
        }

        // Проксирование трафика на настоящий бэкенд (перед которым и стоит WAF)
        proxyToBackend(call)
    }

    private suspend fun proxyToBackend(call: ApplicationCall) {
        // todo
        // делаем запрос к реальному микросервису
        // и возвращаем его ответ в наш call.
        call.respondText("WAF passed. Simulating backend response.")
    }

    override fun close() {
        serviceScope.cancel()
        runBlocking {
            withTimeoutOrNull(5000) {
                serviceScope.coroutineContext[Job]?.join()
            } ?: logger.warn("ServiceScope shutdown timed out")
        }
    }

}