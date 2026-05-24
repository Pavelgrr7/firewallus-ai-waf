package com.pavelryzh.firewallus.infra.db

import com.pavelryzh.firewallus.rule.domain.Action
import com.pavelryzh.firewallus.rule.domain.Condition
import com.pavelryzh.firewallus.rule.domain.Operator
import com.pavelryzh.firewallus.rule.domain.Rule
import com.pavelryzh.firewallus.rule.port.RuleRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import com.pavelryzh.firewallus.rule.domain.Target
import com.pavelryzh.firewallus.rule.event.RuleCacheEvent

@Component
class DefaultRulesSeeder(
    private val ruleRepository: RuleRepository,
    private val eventPublisher: ApplicationEventPublisher,
    // Флаг включения сидера (по умолчанию true)
    @Value($$"${waf.seed.default-rules:true}") private val seedDefaultRules: Boolean
) : ApplicationRunner {

    private val logger = LoggerFactory.getLogger(DefaultRulesSeeder::class.java)

    override fun run(args: ApplicationArguments) {
        if (!seedDefaultRules) {
            logger.info("Сидинг дефолтных правил WAF отключен в конфигурации.")
            return
        }

        // Идемпотентный сидинг
        if (ruleRepository.count() > 0) {
            logger.debug("Правила уже существуют, сидинг дефолтных правил пропущен.")
            return
        }

        logger.info("База правил пуста. Инициализация базового набора правил WAF (OWASP Core Rule Set mini)...")

        val defaultRules = listOf(
            Rule(
                name = "Block SQLi (Basic URI)",
                action = Action.BLOCK,
                isActive = true,
                conditions = listOf(
                    Condition(
                        target = Target.URI,
                        operator = Operator.REGEX,
                        //  union select, drop table, OR 1=1
                        value = "(?i)(union\\s+select|select\\s+.*\\s+from|drop\\s+table|1=1)"
                    )
                )
            ),
            Rule(
                name = "Block XSS (Cross-Site Scripting)",
                action = Action.BLOCK,
                isActive = true,
                conditions = listOf(
                    Condition(
                        target = Target.URI,
                        operator = Operator.REGEX,
                        // теги <script> или псевдопротокол javascript:
                        value = "(?i)(<script.*?>|javascript:)"
                    )
                )
            ),
            Rule(
                name = "Block Path Traversal (LFI)",
                action = Action.BLOCK,
                isActive = true,
                conditions = listOf(
                    Condition(
                        target = Target.URI,
                        operator = Operator.CONTAINS,
                        value = "etc/passwd"
                    )
                )
            ),
            Rule(
                name = "Block Known Scanners (SQLMap)",
                action = Action.BLOCK,
                isActive = true,
                conditions = listOf(
                    Condition(
                        target = Target.HEADER,
                        targetKey = "User-Agent",
                        operator = Operator.CONTAINS,
                        value = "sqlmap"
                    )
                )
            )
        )

        defaultRules.forEach { rule ->
            val savedRule = ruleRepository.save(rule)

            eventPublisher.publishEvent(
                RuleCacheEvent.Saved(
                    ruleId = savedRule.id!!,
                    name = savedRule.name,
                    action = savedRule.action,
                    conditions = savedRule.conditions,
                    isActive = savedRule.isActive
                )
            )
        }

        logger.info("Успешно создано ${defaultRules.size} дефолтных правил защиты.")
    }
}