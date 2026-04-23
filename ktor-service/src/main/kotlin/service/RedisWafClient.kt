package com.pavelryzh.service

import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.async.RedisAsyncCommands
import kotlinx.coroutines.future.await

class RedisWafClient(redisUri: String) : AutoCloseable {

    private val client: RedisClient = RedisClient.create(redisUri)
    // При первом вызове Kotlin сам безопасно инициализирует
    // connection и asyncApi. Если Redis лежит, вылетит исключение,
    // которое поймает runCatching в TrafficService
    // ЭТО Thread-Safe НА 100%
    private val connection: StatefulRedisConnection<String, String> by lazy {
        client.connect()
    }
    private val asyncApi: RedisAsyncCommands<String, String> by lazy {
        connection.async()
    }

    suspend fun isIpBanned(ip: String): Boolean {
        val sanitized = sanitizeIp(ip)
        val keysFound = asyncApi.exists(
            "$BAN_KEY_PREFIX:$sanitized",
            "$MANUAL_BAN_KEY_PREFIX:$sanitized"
        ).await()

        return keysFound > 0L
    }

    suspend fun getActiveRules(): String? {
        return asyncApi.get(ACTIVE_RULES).await()
    }

    override fun close() {
        // Метод shutdown() у Lettuce сам корректно закроет все открытые соединения,
        // если они вообще были созданы
        client.shutdown()
    }

    private fun sanitizeIp(ip: String): String {
        return ip.filter { it.isLetterOrDigit() || it == '.' || it == ':' }
    }
    companion object {
        private const val BAN_KEY_PREFIX = "waf:ban:ip:"
        private const val MANUAL_BAN_KEY_PREFIX = "waf:manual_ban:ip:"
        private const val ACTIVE_RULES = "waf:active_rules"
    }
}
