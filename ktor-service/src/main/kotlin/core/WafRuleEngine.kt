package com.pavelryzh.core

import com.pavelryzh.model.WafRule
import io.ktor.server.application.ApplicationCall

class WafRuleEngine : AutoCloseable {

    // Оценивает запрос. Возвращает сработавшее правило (если есть), иначе null.
    fun evaluate(call: ApplicationCall, cachedBodyString: String?, activeRules: List<WafRule>): WafRule? {
        for (rule in activeRules) {
            // Рекурсивно обходятся вложенные AND, OR и Condition
            if (rule.rootNode.evaluate(call, cachedBodyString)) {
                return rule
            }
        }
        return null
    }

    override fun close() {

    }
}