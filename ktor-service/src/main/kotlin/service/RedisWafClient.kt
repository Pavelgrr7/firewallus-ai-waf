package com.pavelryzh.service

import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.async.RedisAsyncCommands
import kotlinx.coroutines.future.await

class RedisWafClient(redisUri: String) : AutoCloseable {

    private val client: RedisClient = RedisClient.create(redisUri)

    private val connection: StatefulRedisConnection<String, String> = try {
        client.connect()
    } catch (e: Exception) {
        client.shutdown()
        throw e
    }
    private val asyncApi: RedisAsyncCommands<String, String> = try {
        connection.async()
    } catch (e: Exception) {
        connection.close()
        client.shutdown()
        throw e
    }

    suspend fun isIpBanned(ip: String): Boolean {
        val keysFound = asyncApi.exists(
            "$BAN_KEY_PREFIX:$ip",
            "$MANUAL_BAN_KEY_PREFIX:$ip"
        ).await()

        return keysFound > 0L
    }

    suspend fun getActiveRules(): String? {
        return asyncApi.get(ACTIVE_RULES).await()
    }

    override fun close() {
        connection.close()
        client.shutdown()
    }

    companion object {
        private const val BAN_KEY_PREFIX = "waf:ban:ip:"
        private const val MANUAL_BAN_KEY_PREFIX = "waf:manual_ban:ip:"
        private const val ACTIVE_RULES = "waf:active_rules"
    }
}
