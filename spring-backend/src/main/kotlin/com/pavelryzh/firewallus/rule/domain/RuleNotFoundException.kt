package com.pavelryzh.firewallus.rule.domain

class RuleNotFoundException(id: Int): Exception() {
    override val message: String = "Rule with id $id not found."
}