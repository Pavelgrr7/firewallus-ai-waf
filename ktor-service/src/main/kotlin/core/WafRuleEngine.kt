package com.pavelryzh.core

import com.pavelryzh.model.Condition
import com.pavelryzh.model.Operator
import com.pavelryzh.model.WafRule
import com.pavelryzh.model.Target
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.origin
import io.ktor.server.request.httpMethod
import io.ktor.server.request.uri


class WafRuleEngine : AutoCloseable {

    // Оценивает запрос. Возвращает сработавшее правило (если есть), иначе null.
    fun evaluate(call: ApplicationCall, activeRules: List<WafRule>): WafRule? {
        for (rule in activeRules) {
            val isMatch = rule.conditions.all { condition ->
                checkCondition(call, condition)
            }
            if (isMatch) return rule
        }
        return null
    }

    private fun checkCondition(call: ApplicationCall, cond: Condition): Boolean {
        val actualValue = when (cond.target) {
            Target.IP -> call.request.origin.remoteHost
            Target.URI -> call.request.uri
            Target.METHOD -> call.request.httpMethod.value
            Target.HEADER -> call.request.headers[cond.targetKey ?: ""] ?: ""
        }

        return runCatching {
            when (cond.operator) {
                Operator.EQUALS -> actualValue.equals(cond.value, ignoreCase = true)
                Operator.CONTAINS -> actualValue.contains(cond.value, ignoreCase = true)
                Operator.REGEX -> Regex(cond.value, RegexOption.IGNORE_CASE).containsMatchIn(actualValue)
            }
        }.getOrDefault(false) // Защита: если админ ввел кривой Regex, условие просто вернет false
    }

    override fun close() {

    }
}