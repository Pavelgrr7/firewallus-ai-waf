package main.kotlin.com.pavelryzh.firewallus.rule

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import main.kotlin.com.pavelryzh.firewallus.rule.api.RuleType
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

@Entity
@Table(name = "rules")
class Rule(
    @Column(name = "name", nullable = false)
    var name: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false)
    var ruleType: RuleType,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rule_id")
    var id: Int? = null

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    var createdAt: Instant? = null
}