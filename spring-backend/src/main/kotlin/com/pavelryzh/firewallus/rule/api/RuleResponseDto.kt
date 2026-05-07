package main.kotlin.com.pavelryzh.firewallus.rule.api

import main.kotlin.com.pavelryzh.firewallus.rule.Rule

data class RuleResponseDto(
    val id: Int,
    val name: String,
    val ruleType: String,
    val isActive: Boolean
)

fun Rule.toDto(): RuleResponseDto {
    return RuleResponseDto(
        id = this.id,
        name = this.name,
        ruleType = this.ruleType.name,
        isActive = this.isActive
    )
}