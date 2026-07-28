package com.pavelryzh.firewallus.rule.event

import com.pavelryzh.firewallus.rule.domain.Action
import com.pavelryzh.firewallus.rule.domain.RuleNode
import java.util.*

sealed class RuleCacheEvent {
    abstract val adminId: UUID?

    data class Saved(
        val ruleId: Int,
        val name: String,
        val action: Action,
        val rootNode: RuleNode,
        val isActive: Boolean,
        override val adminId: UUID?
    ) : RuleCacheEvent()

    data class Deleted(
        val ruleId: Int,
        override val adminId: UUID?
    ) : RuleCacheEvent()
}