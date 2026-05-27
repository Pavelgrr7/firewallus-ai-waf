package com.pavelryzh.firewallus.infra.cache

import com.pavelryzh.firewallus.blacklist.domain.IpListType
import com.pavelryzh.firewallus.blacklist.port.ManagedIpCache
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

@Component
class RedisManagedIpListenerImpl(
    private val redisTemplate: StringRedisTemplate
) : ManagedIpCache {

    override fun addIp(ipAddress: String, listType: IpListType) {
        val key = getKey(ipAddress, listType)
        // Для ручных банов и вайтлистов TTL не нужен
        // В качестве значения лежит тип списка (для дебага)
        redisTemplate.opsForValue().set(key, listType.name)
    }

    override fun removeIp(ipAddress: String, listType: IpListType) {
        val key = getKey(ipAddress, listType)
        redisTemplate.delete(key)
    }

    private fun getKey(ipAddress: String, listType: IpListType): String {
        return when (listType) {
            IpListType.BLACKLIST -> "waf:manual_ban:ip:$ipAddress"
            IpListType.WHITELIST -> "waf:whitelist:ip:$ipAddress"
        }
    }
}