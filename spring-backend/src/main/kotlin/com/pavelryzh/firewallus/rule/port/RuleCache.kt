package com.pavelryzh.firewallus.rule.port

import com.pavelryzh.firewallus.rule.domain.IpAddress
import com.pavelryzh.firewallus.rule.event.RuleCacheEvent

interface RuleCache {
    fun saveRuleBan(ip: IpAddress, ttlSeconds: Long)
    fun isIpBanned(ip: IpAddress): Boolean
    fun saveRule(event: RuleCacheEvent.Saved)
    fun deleteRule(ruleId: Int)
}