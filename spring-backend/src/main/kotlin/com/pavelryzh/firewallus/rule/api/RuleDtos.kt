package com.pavelryzh.firewallus.rule.api

import com.pavelryzh.firewallus.rule.domain.Action
import com.pavelryzh.firewallus.rule.domain.Rule
import com.pavelryzh.firewallus.rule.domain.RuleNode
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class CreateRuleDto(
    @field:NotBlank val name: String,
    val action: Action,
    @field:NotEmpty val rootNode: RuleNode,
    val isActive: Boolean = true
)

data class RuleResponseDto(
    val id: Int,
    val name: String,
    val isActive: Boolean,
    val action: Action,
    val rootNode: RuleNode,
    )

data class UpdateRuleDto(
    val name: String?,
    val action: Action?,
    val rootNode: RuleNode? = null
)

fun Rule.toDto(): RuleResponseDto {
    return RuleResponseDto(
        id = this.id!!,
        name = this.name,
        isActive = this.isActive,
        action = this.action,
        rootNode = this.rootNode,
    )
}

fun CreateRuleDto.toEntity(): Rule {
    return Rule(
        name = this.name,
        isActive = this.isActive,
        action = this.action,
        rootNode = this.rootNode,
    )
}