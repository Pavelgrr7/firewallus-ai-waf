package com.pavelryzh.firewallus.rule.domain

import com.pavelryzh.firewallus.exception.ResourceNotFoundException

class RuleNotFoundException(id: Int): ResourceNotFoundException("Rule with id $id not found.")