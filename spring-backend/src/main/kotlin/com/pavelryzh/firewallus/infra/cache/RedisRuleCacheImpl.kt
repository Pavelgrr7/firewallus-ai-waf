package com.pavelryzh.firewallus.infra.cache

import tools.jackson.databind.ObjectMapper
import com.pavelryzh.firewallus.rule.domain.IpAddress
import com.pavelryzh.firewallus.rule.port.RuleCache
import com.pavelryzh.firewallus.rule.event.RuleCacheEvent
import com.pavelryzh.firewallus.settings.event.SettingsUpdatedEvent
import com.pavelryzh.firewallus.settings.port.SettingsCache
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RedisCacheAdapter(
    private val redisTemplate: StringRedisTemplate,
    // Импорт правильный, т.к. используется spring boot 4
    private val objectMapper: ObjectMapper
) : RuleCache, SettingsCache {

    private final val ACTIVE_RULES_HASH_KEY = "waf:active_rules"

    override fun saveRuleBan(ip: IpAddress, ttlSeconds: Long) {
        redisTemplate.opsForValue()
            .set("waf:ban:ip:${ip.value}", "banned", Duration.ofSeconds(ttlSeconds))
    }

    override fun isIpBanned(ip: IpAddress): Boolean {
        return redisTemplate.hasKey("waf:ban:ip:${ip.value}")
    }


    override fun saveRule(event: RuleCacheEvent.Saved) {
        val ruleJson = objectMapper.writeValueAsString(event)
        // HSET "waf:active_rules" "5" '{"ruleId": 5, "name": "...", ...}'
        redisTemplate.opsForHash<String, String>()
            .put(ACTIVE_RULES_HASH_KEY, event.ruleId.toString(), ruleJson)
    }

    override fun deleteRule(ruleId: Int) {
        redisTemplate.opsForHash<String, String>()
            .delete(ACTIVE_RULES_HASH_KEY, ruleId.toString())
    }

    override fun saveSettings(event: SettingsUpdatedEvent) {
        val json = objectMapper.writeValueAsString(event)
        redisTemplate.opsForValue().set("waf:global_settings", json)
    }
}