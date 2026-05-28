package com.pavelryzh.firewallus.user.ports

import com.pavelryzh.firewallus.user.domain.Admin
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AdminRepository : JpaRepository<Admin, UUID> {
    fun findByUsername(username: String): Admin?
}