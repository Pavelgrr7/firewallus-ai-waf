package main.kotlin.com.pavelryzh.firewallus.rule

import org.springframework.data.jpa.repository.JpaRepository

interface RuleRepository : JpaRepository<Rule, Long> {
}