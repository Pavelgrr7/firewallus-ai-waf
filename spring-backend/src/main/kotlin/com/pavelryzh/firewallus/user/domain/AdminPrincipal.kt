package com.pavelryzh.firewallus.user.domain

import java.util.UUID

data class AdminPrincipal(
    val id: UUID,
    val username: String
)