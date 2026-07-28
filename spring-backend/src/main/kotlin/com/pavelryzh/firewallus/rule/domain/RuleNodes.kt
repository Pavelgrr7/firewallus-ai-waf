package com.pavelryzh.firewallus.rule.domain

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = AndNode::class, name = "AND"),
    JsonSubTypes.Type(value = OrNode::class, name = "OR"),
    JsonSubTypes.Type(value = NotNode::class, name = "NOT"),
    JsonSubTypes.Type(value = ConditionNode::class, name = "CONDITION")
)
sealed class RuleNode

data class AndNode(
    val children: List<RuleNode> = emptyList()
) : RuleNode()

data class OrNode(
    val children: List<RuleNode> = emptyList()
) : RuleNode()

data class NotNode(
    val child: RuleNode = ConditionNode()
) : RuleNode()

data class ConditionNode(
    val target: Target = Target.IP,
    val targetKey: String? = null,
    val operator: Operator = Operator.EQUALS,
    val value: String = ""
) : RuleNode()