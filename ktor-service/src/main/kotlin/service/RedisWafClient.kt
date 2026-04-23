package com.pavelryzh.service

import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.async.RedisAsyncCommands
import kotlinx.coroutines.future.await

class RedisWafClient(redisUri: String) : AutoCloseable {

    private val client: RedisClient = RedisClient.create(redisUri)
    private val connection: StatefulRedisConnection<String, String>
    private val asyncApi: RedisAsyncCommands<String, String>

    init {
        try {
            connection = client.connect()
            asyncApi = connection.async()
        } catch (e: Exception) {
            client.shutdown()
            throw e
        }
    }

    suspend fun isIpBanned(ip: String): Boolean {
        val keysFound = asyncApi.exists(
            "$BAN_KEY_PREFIX:$ip",
            "$MANUAL_BAN_KEY_PREFIX:$ip"
        ).await()

        return keysFound > 0L
    }

    suspend fun getActiveRules(): String? {
        return asyncApi.get("waf:active_rules").await()
    }

    override fun close() {
        connection.close()
        client.shutdown()
    }

    companion object {
        private const val BAN_KEY_PREFIX = "waf:ban:ip:"
        private const val MANUAL_BAN_KEY_PREFIX = "waf:manual_ban:ip:"
    }
}
