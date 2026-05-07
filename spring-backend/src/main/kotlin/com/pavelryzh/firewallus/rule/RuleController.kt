package main.kotlin.com.pavelryzh.firewallus.rule

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController("/rules")
class RuleController(val ruleService: RuleService) {

    @GetMapping
    fun getAllRules(): List<Rule> {
        val rules = ruleService.getAllRules()
        return (rules)
    }
}