package com.pavelryzh.service

import com.pavelryzh.core.IdentityExtractor
import com.pavelryzh.core.WAF_PAYLOAD_LIMIT_BYTES
import com.pavelryzh.core.WafRuleEngine
import com.pavelryzh.core.extractTrafficLog
import com.pavelryzh.kafka.KafkaTrafficProducer
import com.pavelryzh.model.Action
import com.pavelryzh.model.TargetUrl
import com.pavelryzh.plugins.logger
import com.pavelryzh.service.dto.HttpMethod
import com.pavelryzh.service.dto.IncidentEventDto
import com.pavelryzh.service.dto.parseMethod
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.io.readByteArray

class TrafficService(
    private val kafkaProducer: KafkaTrafficProducer,
    private val redisWafClient: RedisWafClient,
    private val ruleEngine: WafRuleEngine,
    private val httpClient: ProxyHttpClient,
    private val configManager: WafConfigManager
    ) : AutoCloseable {

    private val logger = logger()

    private val exceptionHandler = CoroutineExceptionHandler { _, ex ->
        logger.error("Failed to send traffic log to Kafka", ex)
    }
    private val scopeJob = SupervisorJob()

    private val serviceScope = CoroutineScope(scopeJob + Dispatchers.IO + exceptionHandler)

    suspend fun handleRequest(call: ApplicationCall) {

        val identity = IdentityExtractor.extract(call)
        val ip = identity.ip

        logger.info("handling request, $identity, $ip")

        val settings = configManager.settings.value

        // Проверка белого списка -> проверка банов -> проверка rate limit -> проврка правил

        val isWhitelisted = runCatching { redisWafClient.isWhitelisted(identity.ip) }
            .onFailure { logger.error("Failed to check whitelist for IP ${identity.ip}", it) }
            .getOrDefault(false)
        if (isWhitelisted) {
            logger.debug("IP ${identity.ip} is whitelisted. Bypassing WAF.")
            proxyToBackend(settings.targetUrl, call, null)
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

        val activeRules = configManager.activeRules.value

        val path = call.request.uri.substringBefore('?')
        val isRateLimited = runCatching { redisWafClient.isRateLimited(ip, path, settings.limit, settings.window) }
            .getOrDefault(false) // Fail-Open

        if (isRateLimited) {
            sendIncidentEvent(identity.ip, call.request.uri, "RATE_LIMIT_EXCEEDED", "BLOCK", call, null)

            call.respond(HttpStatusCode.TooManyRequests, "Too Many Requests. WAF Rate Limit Exceeded.")
            return
        }

        val cachedBodyBytes = readCallBody(call)
        val cachedBodyString = cachedBodyBytes?.let { String(it, Charsets.UTF_8) }

        val matchedRule = ruleEngine.evaluate(call,  cachedBodyString, activeRules)

        val trafficLog = extractTrafficLog(call, cachedBodyString)

        if (matchedRule != null) {
            sendIncidentEvent(identity.ip, call.request.uri, matchedRule.name, matchedRule.action.name, call, cachedBodyString)

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
                    proxyToBackend(settings.targetUrl, call, cachedBodyBytes)
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
        proxyToBackend(settings.targetUrl, call, cachedBodyBytes)
    }

    private suspend fun proxyToBackend(targetUrl: TargetUrl, call: ApplicationCall, body: ByteArray?) {
        httpClient.proxyToBackend(targetUrl, call, body)
    }

    private fun sendIncidentEvent(ip: String, uri: String, type: String, action: String, call: ApplicationCall, body: String?) {
        serviceScope.launch {
            val incident = IncidentEventDto(
                incidentType = type,
                attackerIp = ip,
                targetUri = uri,
                actionTaken = action,
                payloadDump = extractTrafficLog(call, body).headers
            )
            kafkaProducer.send(TOPIC_INCIDENT, incident)
        }
    }

    private suspend fun readCallBody(call: ApplicationCall): ByteArray? {

        val contentLength = call.request.headers[HttpHeaders.ContentLength]?.toIntOrNull()
        var cachedBodyBytes: ByteArray? = null

        val method = parseMethod(call.request.httpMethod.value)
        if (method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH) {
            if (contentLength != null && contentLength > 0 && contentLength <= WAF_PAYLOAD_LIMIT_BYTES) {
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