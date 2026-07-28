package com.pavelryzh.service

import com.pavelryzh.model.ClientIdentity
import com.pavelryzh.model.GlobalSettings
import com.pavelryzh.model.WafRule
import com.pavelryzh.plugins.logger
import io.lettuce.core.RedisClient
import io.lettuce.core.ScriptOutputType
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.async.RedisAsyncCommands
import kotlinx.coroutines.future.await
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import java.util.concurrent.atomic.AtomicBoolean

class RedisWafClient(redisUri: String) : AutoCloseable {

    private val client: RedisClient = RedisClient.create(redisUri)
    // При первом вызове Kotlin сам безопасно инициализирует
    // connection и asyncApi. Если Redis лежит, вылетит исключение,
    // которое поймает runCatching в TrafficService
    // ПОНИМАЮ ВСЕ РИСКИ И БЕРУ ОТВЕТСТВЕННОСТЬ ЛИЧНО НА СЕБЯ
    private val connection: StatefulRedisConnection<String, String> by lazy {
        client.connect()
    }
    private val asyncApi: RedisAsyncCommands<String, String> by lazy {
        connection.async()
    }

    private val isClosed = AtomicBoolean(false)

    // автоматическая конвертация camelCase <-> snake_case
    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        namingStrategy = JsonNamingStrategy.SnakeCase
    }

    private val logger = logger()

    @Volatile
    private var rateLimitScriptSha: String? = null

    private val rateLimitScript: String by lazy {
        this::class.java.getResource("/scripts/rate_limit.lua")?.readText()
            ?: throw IllegalStateException("Файл /scripts/rate_limit.lua не найден в resources!")
    }

    suspend fun getActiveRules(): List<WafRule> {
        // HGETALL: Map<String, String>
        val rulesMap = asyncApi.hgetall(ACTIVE_RULES).await()

        return rulesMap.values.mapNotNull { jsonString ->
            logger.debug("Receiving data from redis: $jsonString")
            runCatching {
                json.decodeFromString<WafRule>(jsonString)
            }.onFailure { e ->
                logger.error("Failed to parse WafRule from Redis: ${e.message}")
            }.getOrNull()
        }
    }

    suspend fun getSettings(): GlobalSettings? {
        val jsonString = asyncApi.get(GLOBAL_SETTINGS).await() ?: return null

        return runCatching {
            json.decodeFromString<GlobalSettings>(jsonString)
        }.onFailure { e ->
            logger.error("Failed to parse GlobalSettings from Redis: ${e.message}")
        }.getOrNull()
    }

    suspend fun isRateLimited(ip: String, path: String, limit: Int = 50, windowSeconds: Int = 60): Boolean {
        if (isClosed.get()) throw IllegalStateException("RedisWafClient is closed")

        val key = "waf:ratelimit:ip:${sanitizeIp(ip)}:path:$path"

        // первый запуск
        if (rateLimitScriptSha == null) {
            rateLimitScriptSha = asyncApi.scriptLoad(rateLimitScript).await()
        }

        return try {
            val result = asyncApi.evalsha<Long>(
                rateLimitScriptSha,
                ScriptOutputType.INTEGER,
                arrayOf(key),
                limit.toString(), windowSeconds.toString()
            ).await()

            result == 1L
        } catch (e: Exception) {
            // Защита от NOSCRIPT error
            if (e.message?.contains("NOSCRIPT") == true) {
                rateLimitScriptSha = null
                val fallbackResult = asyncApi.eval<Long>(
                    rateLimitScript,
                    ScriptOutputType.INTEGER,
                    arrayOf(key),
                    limit.toString(), windowSeconds.toString()
                ).await()
                return fallbackResult == 1L
            }
            throw e
        }
    }

    suspend fun isClientBanned(identity: ClientIdentity): Boolean {
        if (isClosed.get()) throw IllegalStateException("RedisWafClient is closed")

        val sanitizedIp = sanitizeIp(identity.ip)
        val keysToCheck = mutableListOf(
            "$BAN_IP_KEY_PREFIX:$sanitizedIp",
            "$MANUAL_BAN_IP_KEY_PREFIX:$sanitizedIp",
            "$FG_BAN_KEY_PREFIX:${identity.fingerprint}"
        )

        identity.jwtHash?.let {
            keysToCheck.add("$JWT_BAN_KEY_PREFIX:$it")
        }

        return runCatching {
            asyncApi.exists(*keysToCheck.toTypedArray()).await()
        }.fold(
            onSuccess = { keysFound ->
                keysFound > 0L // Redis нашел хотя бы 1 ключ - клиент в бане
            },
            onFailure = { ex ->
                if (isClosed.get()) {
                    throw IllegalStateException("RedisWafClient is closed during operation", ex)
                }
                logger.error("Redis connection failed during ban check. Falling back to ALLOW.", ex)
                false // Fail-Open
            }
        )
    }

    suspend fun isWhitelisted(ip: String): Boolean {
        if (isClosed.get()) return false

        return runCatching {
            val sanitizedIp = sanitizeIp(ip)
            asyncApi.exists("$WHITELIST:$sanitizedIp").await()
        }.fold(
            onSuccess = { it > 0L },
            onFailure = { ex ->
                logger.error("Failed to check whitelist for IP $ip", ex)
                false // Если Redis упал, считаем, что IP не в вайтлисте (Fail-Closed для вайтлиста)
            }
        )
    }

    // НА ДАННОМ ЭТАПЕ НЕ НУЖНА ИДЕАЛЬНАЯ PROD-READY ПРОВЕРКА
    // ДОСТАТОЧНО БАЗОВОГО МЕТОДА, КОТОРЫЙ БУДЕТ ДЕТАЛЬНО ПРОРАБОТАН В БУДУЩЕМ
    private fun sanitizeIp(ip: String): String {
        if (IPV4_REGEX.matches(ip) || IPV6_REGEX.matches(ip)) {
            return runCatching {
                java.net.InetAddress.getByName(ip).hostAddress
            }.getOrElse { ip }
        }
        return runCatching {
            java.net.InetAddress.getByName(ip).hostAddress
        }.getOrElse {
            throw IllegalArgumentException("Invalid IP format: $ip")
        }
    }

    override fun close() {
        if (isClosed.compareAndSet(false, true)) {
            connection.close()
            client.shutdown()
        }
    }

    companion object {
        private const val WAF_PREFIX = "waf"
        private const val BAN_PREFIX = "ban"
        private const val BAN_IP_KEY_PREFIX = "$WAF_PREFIX:$BAN_PREFIX:ip"
        private const val MANUAL_BAN_IP_KEY_PREFIX = "$WAF_PREFIX:manual_ban:ip"
        private const val JWT_BAN_KEY_PREFIX = "$WAF_PREFIX:$BAN_PREFIX:jwt"
        private const val FG_BAN_KEY_PREFIX = "$WAF_PREFIX:$BAN_PREFIX:fg"
        private const val ACTIVE_RULES = "$WAF_PREFIX:active_rules"
        private const val GLOBAL_SETTINGS = "$WAF_PREFIX:global_settings"
        private const val WHITELIST = "$WAF_PREFIX:whitelist:ip"

        private val IPV4_REGEX = Regex("""^(\d{1,3}\.){3}\d{1,3}$""")
        private val IPV6_REGEX = Regex("""^([0-9a-fA-F]{0,4}:){2,7}[0-9a-fA-F]{0,4}$""")
    }
}
