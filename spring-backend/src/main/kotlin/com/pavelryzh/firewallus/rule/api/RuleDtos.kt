package com.pavelryzh.firewallus.rule.api

import com.pavelryzh.firewallus.rule.domain.Action
import com.pavelryzh.firewallus.rule.domain.Condition
import com.pavelryzh.firewallus.rule.domain.Rule
import com.pavelryzh.firewallus.rule.domain.RuleType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

data class CreateRuleDto(
    @field:NotBlank val name: String,
    val action: Action,
    @field:NotEmpty val conditions: List<Condition>,
    val isActive: Boolean = true
)

data class RuleResponseDto(
    val id: Int,
    val name: String,
    val isActive: Boolean,
    val action: Action,
    val conditions: List<Condition>
    )

data class UpdateRuleDto(
    val name: String?,
    val action: Action?,
    val conditions: List<Condition>? = null
)

fun Rule.toDto(): RuleResponseDto {
    return RuleResponseDto(
        id = this.id!!,
        name = this.name,
        isActive = this.isActive,
        action = this.action,
        conditions = this.conditions,
    )
}

fun CreateRuleDto.toEntity(): Rule {
    return Rule(
        name = this.name,
        isActive = this.isActive,
        action = this.action,
        conditions = this.conditions,
    )
}