package com.pavelryzh.firewallus.rule.event

import com.pavelryzh.firewallus.rule.domain.RuleType

sealed class RuleCacheEvent {

    data class Saved(
        val ruleId: Int,
        val name: String,
        val ruleType: RuleType,
        val isActive: Boolean
    ) : RuleCacheEvent()

    data class Deleted(
        val ruleId: Int
    ) : RuleCacheEvent()
}