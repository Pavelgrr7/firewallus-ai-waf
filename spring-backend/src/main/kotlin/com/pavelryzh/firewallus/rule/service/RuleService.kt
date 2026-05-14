package com.pavelryzh.firewallus.rule.service

import com.pavelryzh.firewallus.rule.event.RuleCacheEvent
import com.pavelryzh.firewallus.rule.api.CreateRuleDto
import com.pavelryzh.firewallus.rule.api.UpdateRuleDto
import com.pavelryzh.firewallus.rule.api.toEntity
import com.pavelryzh.firewallus.rule.domain.Rule
import com.pavelryzh.firewallus.rule.domain.RuleNotFoundException
import com.pavelryzh.firewallus.rule.port.RuleRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RuleService(private val ruleRepo: RuleRepository, private val eventPublisher: ApplicationEventPublisher) {

    @Transactional(readOnly = true)
    fun getAllRules(pageable: Pageable): Page<Rule> {
        return ruleRepo.findAll(pageable)
    }

    @Transactional
    fun disableRule(id: Int): Rule {
        val rule: Rule = ruleRepo.findById(id).orElseThrow { RuleNotFoundException(id) }
        rule.isActive = false
        publishSavedEvent(rule)
        return rule
    }
    @Transactional(readOnly = true)
    fun getRuleById(id: Int): Rule {
        return ruleRepo.findById(id).orElseThrow { RuleNotFoundException(id) }
    }
    @Transactional
    fun createRule(ruleDto: CreateRuleDto): Rule {
        val rule = ruleRepo.save(ruleDto.toEntity())
        publishSavedEvent(rule)
        return rule
    }
    @Transactional
    fun updateRule(id: Int, updateDto: UpdateRuleDto): Rule {
        val rule = ruleRepo.findById(id).orElseThrow { RuleNotFoundException(id) }
        updateDto.name?.let { rule.name = it }
        updateDto.action?.let { rule.action = it }
        updateDto.conditions?.let { rule.conditions = it }
        publishSavedEvent(rule)
        return rule
    }

    @Transactional
    fun deleteRule(id: Int) {
        val rule = ruleRepo.findById(id).orElseThrow { RuleNotFoundException(id) }
        ruleRepo.delete(rule)
        publishDeletedEvent(id)
    }

    @Transactional
    fun enableRule(id: Int): Rule {
        val rule: Rule = ruleRepo.findById(id).orElseThrow { RuleNotFoundException(id) }
        rule.isActive = true
        publishSavedEvent(rule)

        return rule

    }

    private fun publishSavedEvent(rule: Rule) {
        val event = RuleCacheEvent.Saved(
            ruleId = rule.id!!,
            name = rule.name,
            action = rule.action,
            conditions = rule.conditions,
            isActive = rule.isActive
        )
        eventPublisher.publishEvent(event)
    }

    private fun publishDeletedEvent(id: Int) {
        eventPublisher.publishEvent(RuleCacheEvent.Deleted(id))
    }
}