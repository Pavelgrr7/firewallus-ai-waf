package com.pavelryzh.firewallus.incident.domain

import com.pavelryzh.firewallus.rule.domain.IpAddress
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "incident_logs")
class Incident(
    @Column(name = "incident_type", nullable = false)
    var incidentType: String,

    @Column(name = "attacker_ip", nullable = false)
    var attackerIp: IpAddress,

    @Column(name = "target_uri", nullable = false)
    var targetUri: String,

    @Column(name = "action_taken", nullable = false)
    var actionTaken: String,

    // Скор от ML. Nullable, так как для статических правил от Ktor его нет
    @Column(name = "confidence_score")
    var confidenceScore: Float? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload_dump", columnDefinition = "jsonb", nullable = false)
    var payloadDump: Map<String, String>
) {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "incident_id")
    var id: UUID? = null

    @CreationTimestamp
    @Column(name = "timestamp", updatable = false)
    var timestamp: Instant? = null
}