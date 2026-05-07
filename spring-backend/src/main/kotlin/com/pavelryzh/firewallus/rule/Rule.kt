package main.kotlin.com.pavelryzh.firewallus.rule

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.Date

@Entity
@Table(name = "rules")
class Rule {
    @Id
    @Column(name = "rule_id")
    var id: Int? = null

    @Column(name = "name")
    var name: String? = null

    @Column(name = "is_active")
    var isActive: Boolean? = null

    @Column(name = "created_at")
    var createdAt: Date? = null

}