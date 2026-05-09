package com.pavelryzh.firewallus.rule.api

import jakarta.validation.Valid
import com.pavelryzh.firewallus.rule.service.RuleService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/rules")
class RuleController(
    private val ruleService: RuleService
) {

    @GetMapping
    fun getAllRules(): List<RuleResponseDto> {
        val rules = ruleService.getAllRules()
        return rules.map { it.toDto() }
    }

    @GetMapping("/{id}")
    fun getRule(@PathVariable id: Int): RuleResponseDto {
        return ruleService.getRuleById(id).toDto()
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createRule(@Valid @RequestBody ruleDto: CreateRuleDto): RuleResponseDto {
        val newRule = ruleService.createRule(ruleDto)
        return newRule.toDto()
    }

    @PatchMapping("/{id}")
    fun updateRuleById(
        @PathVariable id: Int,
        @RequestBody updateDto: UpdateRuleDto
    ): RuleResponseDto {
        return ruleService.updateRule(id, updateDto).toDto()
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteRuleById(@PathVariable id: Int) {
        ruleService.deleteRule(id)
    }

    // Pragmatic REST Actions

    @PostMapping("/{id}/enable")
    fun enableRule(@PathVariable id: Int): RuleResponseDto {
        val activatedRule = ruleService.enableRule(id)
        return activatedRule.toDto()
    }

    @PostMapping("/{id}/disable")
    fun disableRule(@PathVariable id: Int): RuleResponseDto {
        val deactivatedRule = ruleService.disableRule(id)
        return deactivatedRule.toDto()
    }
}