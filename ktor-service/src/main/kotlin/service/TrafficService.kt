package com.pavelryzh.service

import com.pavelryzh.core.IdentityExtractor
import com.pavelryzh.core.WafRuleEngine
import com.pavelryzh.kafka.KafkaTrafficProducer
import com.pavelryzh.model.Action
import com.pavelryzh.model.WafRule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.pavelryzh.plugins.logger
import com.pavelryzh.routes.extractTrafficLog
import com.pavelryzh.model.GlobalSettings
import com.pavelryzh.service.dto.IncidentEventDto
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.origin
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull

class TrafficService(
    private val kafkaProducer: KafkaTrafficProducer,
    private val redisWafClient: RedisWafClient,
    private val ruleEngine: WafRuleEngine
    ) : AutoCloseable {

    private val logger = logger()

    private val exceptionHandler = CoroutineExceptionHandler { _, ex ->
        logger.error("Failed to send traffic log to Kafka", ex)
    }
    private val scopeJob = SupervisorJob()

    private val serviceScope = CoroutineScope(scopeJob + Dispatchers.IO + exceptionHandler)

    @Volatile
    private var settings: GlobalSettings = GlobalSettings()

    @Volatile
    private var activeRules: List<WafRule> = emptyList()

    init {
        // Корутина раз в 10 секунд фетчит правила из Redis
        serviceScope.launch {
            while (isActive) {
                activeRules = runCatching { redisWafClient.getActiveRules() }
                    .getOrDefault(activeRules) // Redis упал -> старые правила

                settings = runCatching { redisWafClient.getSettings() }
                    .getOrNull() ?: settings

                delay(10_000)
            }
        }
    }

    suspend fun handleRequest(call: ApplicationCall) {
        val ip = call.request.origin.remoteHost

        val identity = IdentityExtractor.extract(call)

        // Fail-Open: если Redis недоступен, разрешаем трафик (безопаснее, чем Fail-Closed)
        val isBanned = runCatching { redisWafClient.isClientBanned(identity) }
            .onFailure { logger.error("Redis is down, allowing traffic (Fail-Open)", it) }
            .getOrDefault(false)

        if (isBanned) {
            call.respond(HttpStatusCode.Forbidden, "Access Denied by WAF")
            return
        }

        val isRateLimited = runCatching { redisWafClient.isRateLimited(ip, settings.limit, settings.window) }
            .getOrDefault(false) // Fail-Open

        if (isRateLimited) {
            val incident = IncidentEventDto(
                incidentType = "RATE_LIMIT_EXCEEDED",
                attackerIp = ip,
                targetUri = call.request.uri,
                actionTaken = "BLOCK",
                headersDump = extractTrafficLog(call).headers
            )

            serviceScope.launch { kafkaProducer.send("incidents", incident) }

            call.respond(HttpStatusCode.TooManyRequests, "Too Many Requests. WAF Rate Limit Exceeded.")
            return
        }

        val matchedRule = ruleEngine.evaluate(call, activeRules)

        val trafficLog = extractTrafficLog(call)

        if (matchedRule != null) {
            val incident = IncidentEventDto(
                incidentType = matchedRule.name,
                attackerIp = ip,
                targetUri = call.request.uri,
                actionTaken = matchedRule.action.name,
                headersDump = trafficLog.headers
            )
            serviceScope.launch { kafkaProducer.send(TOPIC_INCIDENT, incident) }

            // Выполняем действие правила
            when (matchedRule.action) {
                Action.BLOCK -> {
                    call.respond(HttpStatusCode.Forbidden, "WAF Blocked Request")
                    return
                }
                Action.LOG -> {
                    logger.info("Rule ${matchedRule.name} matched in LOG mode for IP $ip")
                }
                Action.ALLOW -> {
                    logger.info("Rule ${matchedRule.name} matched in ALLOW mode for IP $ip")
                    proxyToBackend(call)
                    return
                }
            }
        }
        serviceScope.launch {
            kafkaProducer.send(
                topic = TOPIC_TRAFFIC,
                trafficLog,

            )
        }
        proxyToBackend(call)
    }

    private suspend fun proxyToBackend(call: ApplicationCall) {
        // todo
        // делаем запрос к реальному микросервису
        // и возвращаем его ответ в наш call.
        call.respondText("WAF passed. Simulating backend response.")
    }

    override fun close() {
        runBlocking {
            scopeJob.cancel()
            withTimeoutOrNull(5000) {
                scopeJob.join()
            } ?: run {
                logger.error("ServiceScope shutdown timed out, forcing cancellation")
                scopeJob.cancelChildren()
            }
        }
    }

    companion object {
        const val TOPIC_TRAFFIC = "traffic-logs"
        const val TOPIC_INCIDENT = "incidents"
    }
}