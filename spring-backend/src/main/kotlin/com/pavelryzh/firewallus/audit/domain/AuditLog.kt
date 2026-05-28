package com.pavelryzh.firewallus.audit.domain


import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.UUID

enum class AuditAction {
    // Правила
    CREATE_RULE, UPDATE_RULE, DELETE_RULE, ENABLE_RULE, DISABLE_RULE, SEED_DEFAULT_RULES,

    // Списки доступа
    ADD_MANAGED_IP, REMOVE_MANAGED_IP,

    // Настройки
    UPDATE_SETTINGS,

    // Авторизация и пользователи
    LOGIN, CREATE_ADMIN, DELETE_ADMIN, CHANGE_PASSWORD
}

@Entity
@Table(name = "audit_logs")
class AuditLog(
    @Column(name = "admin_id")
    var adminId: UUID?,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "action", nullable = false)
    var action: AuditAction,

    @Column(name = "rule_id")
    var ruleId: Int?,

    @Column(name = "rule_name", nullable = false)
    var ruleName: String
) {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "action_id")
    var id: UUID? = null

    @CreationTimestamp
    @Column(name = "timestamp", updatable = false)
    var timestamp: Instant? = null
}