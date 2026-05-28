package com.pavelryzh.model
import kotlinx.serialization.Serializable

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
    val conditions: List<Condition>,
    val isActive: Boolean
)