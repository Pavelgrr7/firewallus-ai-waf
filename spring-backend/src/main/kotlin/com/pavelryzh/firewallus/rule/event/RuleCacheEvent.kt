package com.pavelryzh.firewallus.rule.event

import com.pavelryzh.firewallus.rule.domain.Action
import com.pavelryzh.firewallus.rule.domain.Condition
import java.util.UUID

sealed class RuleCacheEvent {
    abstract val adminId: UUID?

    data class Saved(
        val ruleId: Int,
        val name: String,
        val action: Action,
        val conditions: List<Condition>,
        val isActive: Boolean,
        override val adminId: UUID?
    ) : RuleCacheEvent()

    data class Deleted(
        val ruleId: Int,
        override val adminId: UUID?
    ) : RuleCacheEvent()
}