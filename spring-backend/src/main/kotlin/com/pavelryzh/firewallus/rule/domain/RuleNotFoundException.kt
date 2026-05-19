package com.pavelryzh.firewallus.rule.domain

import com.pavelryzh.firewallus.api.ResourceNotFoundException

class RuleNotFoundException(id: Int): ResourceNotFoundException("Rule with id $id not found.")