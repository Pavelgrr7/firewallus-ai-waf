package main.kotlin.com.pavelryzh.firewallus.rule.api

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateRuleDto(

    @field:NotBlank(message = "Имя правила не может быть пустым")
    @field:Size(max = 64, message = "Имя правила не должно превышать 64 символа")
    val name: String,

    val ruleType: RuleType,

    @field:NotBlank(message = "Условие не может быть пустым")
    val conditionValue: String
)