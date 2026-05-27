package com.pavelryzh.firewallus.rule.api

import com.pavelryzh.firewallus.infra.db.DefaultRulesSeeder
import com.pavelryzh.firewallus.rule.service.RuleService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/rules")
@PreAuthorize("hasRole('ADMIN')")
class RuleController(
    private val ruleService: RuleService,
    private val defaultRulesSeederService: DefaultRulesSeeder
) {

    @GetMapping
    fun getAllRules(
        @PageableDefault(size = 20, page = 0) pageable: Pageable
    ): Page<RuleResponseDto> {

        val rulesPage = ruleService.getAllRules(pageable)
        return rulesPage.map { it.toDto() }
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


    @PostMapping("/seed-defaults")
    fun seedDefaultRules(): Map<String, String> {
        defaultRulesSeederService.seed()
        return mapOf("message" to "Default rules successfully created")
    }
}