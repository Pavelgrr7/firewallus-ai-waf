package com.pavelryzh.firewallus.rule.port

import com.pavelryzh.firewallus.rule.domain.Rule
import org.springframework.data.jpa.repository.JpaRepository

interface RuleRepository : JpaRepository<Rule, Int> {
}