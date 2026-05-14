package com.pavelryzh.firewallus.rule.domain

enum class Action { BLOCK, ALLOW, LOG }
enum class Target { IP, URI, HEADER, METHOD }
enum class Operator { EQUALS, CONTAINS, REGEX }

data class Condition(
    val target: Target = Target.IP,
    val targetKey: String? = null,
    val operator: Operator = Operator.EQUALS,
    val value: String = ""
)