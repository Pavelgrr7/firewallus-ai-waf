package com.pavelryzh.service

import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.async.RedisAsyncCommands
import kotlinx.coroutines.future.await

class RedisWafClient(redisUri: String) : AutoCloseable {

    private val client: RedisClient = RedisClient.create(redisUri)
    private val connection: StatefulRedisConnection<String, String> = client.connect()

    private val asyncApi: RedisAsyncCommands<String, String> = connection.async()

    suspend fun isIpBanned(ip: String): Boolean {
        val keysFound = asyncApi.exists(
            "waf:ban:ip:$ip",
            "waf:manual_ban:ip:$ip"
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
}