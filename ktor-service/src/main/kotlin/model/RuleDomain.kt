package com.pavelryzh.model
import com.pavelryzh.core.normalizePayload
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.origin
import io.ktor.server.request.httpMethod
import io.ktor.server.request.uri
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
enum class Action { BLOCK, ALLOW, LOG }

@Serializable
enum class Target { IP, URI, HEADER, METHOD, BODY }

@Serializable
enum class Operator { EQUALS, CONTAINS, REGEX }

@Serializable
data class Condition(
    val target: Target,
    val targetKey: String? = null,
    val operator: Operator,
    val value: String
)
@Serializable
data class WafRule(
    val ruleId: Long,
    val name: String,
    val action: Action,
    val rootNode: RuleNode,
    val isActive: Boolean
)

// Базовый класс для ВСЕХ узлов дерева
@Serializable
sealed class RuleNode {
    abstract fun evaluate(call: ApplicationCall, cachedBodyString: String?): Boolean
}

@Serializable
@SerialName("AND")
data class AndNode(val children: List<RuleNode>) : RuleNode() {
    override fun evaluate(call: ApplicationCall, cachedBodyString: String?): Boolean {
        return children.all { it.evaluate(call, cachedBodyString) }
    }
}

@Serializable
@SerialName("OR")
data class OrNode(val children: List<RuleNode>) : RuleNode() {
    override fun evaluate(call: ApplicationCall, cachedBodyString: String?): Boolean {
        return children.any { it.evaluate(call, cachedBodyString) }
    }
}

// 3. УЗЕЛ "НЕ" (NOT) - Инвертирует результат дочернего узла
@Serializable
@SerialName("NOT")
data class NotNode(val child: RuleNode) : RuleNode() {
    override fun evaluate(call: ApplicationCall, cachedBodyString: String?): Boolean {
        return !child.evaluate(call, cachedBodyString)
    }
}

@Serializable
@SerialName("CONDITION")
data class ConditionNode(
    val target: Target,
    val targetKey: String? = null,
    val operator: Operator,
    val value: String
) : RuleNode() {
    override fun evaluate(call: ApplicationCall, cachedBodyString: String?): Boolean {
        val actualValue = when (target) {
            Target.IP -> call.request.origin.remoteHost
            Target.URI -> normalizePayload(call.request.uri)
            Target.METHOD -> call.request.httpMethod.value
            Target.HEADER -> normalizePayload(call.request.headers[targetKey ?: ""] ?: "")
            Target.BODY -> normalizePayload(cachedBodyString ?: "")
        }

        return runCatching {
            when (operator) {
                Operator.EQUALS -> actualValue.equals(value, ignoreCase = true)
                Operator.CONTAINS -> actualValue.contains(value, ignoreCase = true)
                Operator.REGEX -> Regex(value, RegexOption.IGNORE_CASE).containsMatchIn(actualValue)
            }
        }.getOrDefault(false)
    }
}