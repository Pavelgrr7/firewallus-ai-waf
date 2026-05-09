package com.pavelryzh.firewallus.rule.api

import com.pavelryzh.firewallus.rule.domain.Rule
import com.pavelryzh.firewallus.rule.domain.RuleType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateRuleDto(

    @field:NotBlank(message = "Имя правила не может быть пустым")
    @field:Size(max = 64, message = "Имя правила не должно превышать 64 символа")
    val name: String,

    val ruleType: RuleType,

    @field:Size(max = 255, message = "Условие должно превышать 255 символов")
    val conditionValue: String,

    val isActive: Boolean = false
)

data class RuleResponseDto(
    val id: Int,
    val name: String,
    val ruleType: String,
    val isActive: Boolean,
    val conditionValue: String? = "",
)

data class UpdateRuleDto(
    val name: String?,
    val ruleType: RuleType?,
    val conditionValue: String?
    )

fun Rule.toDto(): RuleResponseDto {
    return RuleResponseDto(
        id = this.id!!,
        name = this.name,
        ruleType = this.ruleType.name,
        isActive = this.isActive,
        conditionValue = this.conditionValue
    )
}

fun CreateRuleDto.toEntity(): Rule {
    return Rule(
        name = this.name,
        ruleType = this.ruleType,
        isActive = this.isActive,
        conditionValue = this.conditionValue
    )
}