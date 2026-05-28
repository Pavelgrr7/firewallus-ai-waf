package com.pavelryzh.firewallus.user.event

import java.util.UUID

data class AdminLoginEvent(
    val adminId: UUID,
    val username: String
)