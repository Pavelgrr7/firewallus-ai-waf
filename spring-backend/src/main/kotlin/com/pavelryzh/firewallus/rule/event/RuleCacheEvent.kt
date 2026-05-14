package com.pavelryzh.firewallus.rule.event

import com.pavelryzh.firewallus.rule.domain.Action
import com.pavelryzh.firewallus.rule.domain.Condition

sealed class RuleCacheEvent {

    data class Saved(
        val ruleId: Int,
        val name: String,
        val action: Action,
        val conditions: List<Condition>,
        val isActive: Boolean
    ) : RuleCacheEvent()

    data class Deleted(
        val ruleId: Int
    ) : RuleCacheEvent()
}