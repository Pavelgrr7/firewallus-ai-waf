package main.kotlin.com.pavelryzh.firewallus.rule

import main.kotlin.com.pavelryzh.firewallus.IpAddress

interface RuleCache {
    fun getAllRules(): List<Rule>
    fun saveRuleBan(ip: IpAddress, ttlSeconds: Long)
    fun isIpBanned(ip: IpAddress): Boolean
}