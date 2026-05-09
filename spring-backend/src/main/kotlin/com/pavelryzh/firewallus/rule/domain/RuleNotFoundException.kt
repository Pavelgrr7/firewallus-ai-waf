package com.pavelryzh.firewallus.rule.domain

class RuleNotFoundException(id: Int): RuntimeException("Rule with id $id not found.")