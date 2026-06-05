package com.pavelryzh.firewallus.api


import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/metrics")
@PreAuthorize("hasRole('ADMIN')")
class MetricsController(
    private val redisTemplate: StringRedisTemplate
) {

    @GetMapping("/redis-rate")
    fun getRedisRate(): Map<String, String> {
        val info = redisTemplate.execute { connection ->
            connection.serverCommands().info("stats")
        }

        val opsPerSec = info?.getProperty("instantaneous_ops_per_sec") ?: "0"

        return mapOf("rate" to opsPerSec)
    }
}