package com.pavelryzh.service

import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.async.RedisAsyncCommands
import kotlinx.coroutines.future.await
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

    suspend fun isIpBanned(ip: String): Boolean {
        if (isClosed.get()) throw IllegalStateException("RedisWafClient is closed")

        return runCatching {
            val sanitized = sanitizeIp(ip)
            val keysFound = asyncApi.exists(
                "$BAN_KEY_PREFIX:$sanitized",
                "$MANUAL_BAN_KEY_PREFIX:$sanitized"
            ).await()
            keysFound > 0L
        }.getOrElse { ex ->
            if (isClosed.get()) {
                throw IllegalStateException("RedisWafClient is closed", ex)
            }
            throw ex
        }
    }

    suspend fun getActiveRules(): String? {
        return asyncApi.get(ACTIVE_RULES).await()
    }

    override fun close() {
        if (isClosed.compareAndSet(false, true)) {
            connection.close()
            client.shutdown()
        }
    }

    private fun sanitizeIp(ip: String): String {
        return runCatching {
            java.net.InetAddress.getByName(ip).hostAddress
        }.getOrElse {
            ip.filter { it.isLetterOrDigit() || it == '.' || it == ':' }
        }
    }
    companion object {
        private const val WAF_PREFIX = "waf"
        private const val BAN_KEY_PREFIX = "$WAF_PREFIX:ban:ip:"
        private const val MANUAL_BAN_KEY_PREFIX = "$WAF_PREFIX:manual_ban:ip:"
        private const val ACTIVE_RULES = "$WAF_PREFIX:active_rules"
    }
}
