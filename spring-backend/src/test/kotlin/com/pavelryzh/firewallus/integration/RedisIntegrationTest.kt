package com.pavelryzh.firewallus.integration

import com.pavelryzh.firewallus.blacklist.domain.IpListType
import com.pavelryzh.firewallus.blacklist.service.ManagedIpService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.data.redis.core.StringRedisTemplate
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

class KRedisContainer(imageName: String) : GenericContainer<KRedisContainer>(imageName)

@SpringBootTest
@Testcontainers
class RedisIntegrationTest {

    companion object {
        @Container
        @ServiceConnection
        val postgres = PostgreSQLContainer("postgres:17-alpine")

        @Container
        @ServiceConnection(name = "redis")
        val redis = KRedisContainer("redis:alpine")
            .withExposedPorts(6379)!!
    }

    @Autowired
    lateinit var managedIpService: ManagedIpService

    @Autowired
    lateinit var redisTemplate: StringRedisTemplate

    @Test
    fun `when adding IP to blacklist, it should be saved in DB and pushed to Redis`() {
        val testIp = "10.20.30.40"

        managedIpService.addIp(testIp, IpListType.BLACKLIST, "Integration Test Hacker")

        // "waf:manual_ban:ip:$ip"
        val redisKey = "waf:manual_ban:ip:$testIp"

        assertTrue(redisTemplate.hasKey(redisKey), "Key must exist in Redis")
        assertEquals("BLACKLIST", redisTemplate.opsForValue().get(redisKey))

        managedIpService.removeIpByAddress(testIp)
    }
}