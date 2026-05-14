package com.pavelryzh.firewallus.rule.domain

enum class Action { BLOCK, ALLOW, LOG }
enum class Target { IP, URI, HEADER, METHOD }
enum class Operator { EQUALS, CONTAINS, REGEX }

data class Condition(
    val target: Target,
    val targetKey: String? = null, // Заполняется только если target == HEADER
    val operator: Operator,
    val value: String
)