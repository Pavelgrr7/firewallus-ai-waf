package com.pavelryzh.firewallus.user.domain

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "admins")
class Admin(
    @Column(name = "username", nullable = false, unique = true)
    val username: String,

    @Column(name = "password_hash", nullable = false)
    val passwordHash: String
) {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "admin_id")
    var id: UUID? = null
}