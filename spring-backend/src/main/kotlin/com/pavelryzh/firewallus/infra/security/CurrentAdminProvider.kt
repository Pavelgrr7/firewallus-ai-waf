package com.pavelryzh.firewallus.infra.security

import com.pavelryzh.firewallus.user.domain.AdminPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CurrentAdminProvider {

    fun getCurrentAdminId(): UUID? {
        val authentication = SecurityContextHolder.getContext().authentication ?: return null

        val principal = authentication.principal as? AdminPrincipal ?: return null

        return principal.id
    }
}