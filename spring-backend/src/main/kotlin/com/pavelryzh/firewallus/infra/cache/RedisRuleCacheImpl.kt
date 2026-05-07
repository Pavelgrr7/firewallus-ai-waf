package main.kotlin.com.pavelryzh.firewallus.infra.cache


import main.kotlin.com.pavelryzh.firewallus.IpAddress
import main.kotlin.com.pavelryzh.firewallus.rule.Rule
import main.kotlin.com.pavelryzh.firewallus.rule.RuleCache
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import kotlin.Long
import java.time.Duration

@Component
class RedisRuleCacheAdapter(
    private val redisTemplate: StringRedisTemplate
) : RuleCache {

    override fun saveRuleBan(ip: IpAddress, ttlSeconds: Long) {
        redisTemplate.opsForValue().set("waf:ban:ip:${ip.value}", "banned", Duration.ofSeconds(ttlSeconds))
    }

    override fun isIpBanned(ip: IpAddress): Boolean {
        return redisTemplate.hasKey("waf:ban:ip:${ip.value}")
    }

    override fun getAllRules(): List<Rule> {
        TODO("Not yet implemented")
    }
}