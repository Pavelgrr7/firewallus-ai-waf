package com.pavelryzh.service

import com.pavelryzh.core.IdentityExtractor
import com.pavelryzh.core.WAF_PAYLOAD_LIMIT_BYTES
import com.pavelryzh.core.WafRuleEngine
import com.pavelryzh.kafka.KafkaTrafficProducer
import com.pavelryzh.model.Action
import com.pavelryzh.model.WafRule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.pavelryzh.plugins.logger
import com.pavelryzh.core.extractTrafficLog
import com.pavelryzh.model.GlobalSettings
import com.pavelryzh.service.dto.HttpMethod
import com.pavelryzh.service.dto.IncidentEventDto
import com.pavelryzh.service.dto.parseMethod
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.httpMethod
import io.ktor.server.request.receiveChannel
import io.ktor.server.request.receiveText
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.io.readByteArray

class TrafficService(
    private val kafkaProducer: KafkaTrafficProducer,
    private val redisWafClient: RedisWafClient,
    private val ruleEngine: WafRuleEngine,
    private val httpClient: ProxyHttpClient,
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

        val identity = IdentityExtractor.extract(call)
        val ip = identity.ip

        // Проверка белого списка -> проверка банов -> проверка rate limit -> проврка правил

        val isWhitelisted = redisWafClient.isWhitelisted(identity.ip)
        if (isWhitelisted) {
            logger.debug("IP ${identity.ip} is whitelisted. Bypassing WAF.")
            proxyToBackend(call, null)
            return
        }
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
            sendIncidentEvent(identity.ip, call.request.uri, "RATE_LIMIT_EXCEEDED", "BLOCK", call)

            call.respond(HttpStatusCode.TooManyRequests, "Too Many Requests. WAF Rate Limit Exceeded.")
            return
        }

        val cachedBodyBytes = readCallBody(call)
        val cachedBodyString = cachedBodyBytes?.let { String(it, Charsets.UTF_8) }

        val matchedRule = ruleEngine.evaluate(call,  cachedBodyString, activeRules)

        val trafficLog = extractTrafficLog(call, cachedBodyString)

        if (matchedRule != null) {
            sendIncidentEvent(identity.ip, call.request.uri, matchedRule.name, matchedRule.action.name, call)

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
                    proxyToBackend(call, cachedBodyBytes)
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
        proxyToBackend(call, cachedBodyBytes)
    }

    private suspend fun proxyToBackend(call: ApplicationCall, body: ByteArray?) {
        httpClient.proxyToBackend(call, body)
    }

    private fun sendIncidentEvent(ip: String, uri: String, type: String, action: String, call: ApplicationCall) {
        serviceScope.launch {
            val incident = IncidentEventDto(
                incidentType = type,
                attackerIp = ip,
                targetUri = uri,
                actionTaken = action,
                headersDump = extractTrafficLog(call).headers
            )
            kafkaProducer.send(TOPIC_INCIDENT, incident)
        }
    }

    private suspend fun readCallBody(call: ApplicationCall): ByteArray? {

        val contentLength = call.request.headers[HttpHeaders.ContentLength]?.toIntOrNull()
        var cachedBodyBytes: ByteArray? = null

        val method = parseMethod(call.request.httpMethod.value)
        if (method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH) {
            if (contentLength != null && contentLength <= WAF_PAYLOAD_LIMIT_BYTES) {
                cachedBodyBytes = call.receiveChannel().readRemaining().readByteArray()
            } else if (contentLength == null) {
                logger.warn("Chunked or missing Content-Length. Skipping payload inspection.")
            } else {
                logger.info("Payload too large ($contentLength bytes). Skipping WAF inspection.")
            }
        }

        return cachedBodyBytes
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