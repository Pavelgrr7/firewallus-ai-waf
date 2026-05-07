package main.kotlin.com.pavelryzh.firewallus.rule

import org.springframework.stereotype.Service

@Service
class RuleService(val cache: RuleCache, val ruleRepo: RuleRepository) {

    fun getAllRules(): List<Rule> {
        return cache.getAllRules()
    }
}