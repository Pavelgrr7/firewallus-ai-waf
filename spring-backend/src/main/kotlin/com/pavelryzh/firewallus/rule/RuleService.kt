package main.kotlin.com.pavelryzh.firewallus.rule

import main.kotlin.com.pavelryzh.firewallus.rule.api.CreateRuleDto
import main.kotlin.com.pavelryzh.firewallus.rule.api.UpdateRuleDto
import org.springframework.stereotype.Service

@Service
class RuleService(val cache: RuleCache, val ruleRepo: RuleRepository) {

    fun getAllRules(): List<Rule> {
        return cache.getAllRules()
    }

    fun disableRule(id: Int): Rule {
        TODO()
    }
    fun getRuleById(id: Int): Rule {
        TODO()
    }

    fun createRule(ruleDto: CreateRuleDto): Rule {
        TODO()
    }

    fun updateRule(id: Int, updateDto: UpdateRuleDto): Rule {
        TODO()
    }

    fun deleteRule(id: Int): Unit {

    }

    fun enableRule(id: Int): Rule {
        TODO()
    }
}