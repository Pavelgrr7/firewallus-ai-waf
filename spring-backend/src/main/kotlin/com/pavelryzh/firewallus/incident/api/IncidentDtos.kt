package com.pavelryzh.firewallus.incident.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.pavelryzh.firewallus.incident.domain.Incident
import com.pavelryzh.firewallus.rule.domain.IpAddress
import java.time.Instant

@JsonIgnoreProperties(ignoreUnknown = true)
data class IncidentEventDto(
    val incidentType: String,
    val attackerIp: String,
    val confidenceScore: Float? = null,
    val targetUri: String? = null,
    val actionTaken: String? = null,

    @JsonProperty("payload_dump")
    val payloadDump: Map<String, String>? = null
) {
    init {
        require(incidentType.isNotBlank()) { "incidentType cannot be blank" }
        require(attackerIp.isNotBlank()) { "attackerIp cannot be blank" }
    }
}

data class IncidentResponseDto(
    val incidentType: String,
    val attackerIp: String,
    val targetUri: String,
    val actionTaken: String,
    val confidenceScore: Float? = null,
    val timestamp: Instant? = null,
)

fun IncidentEventDto.toEntity(): Incident = Incident(
        incidentType = this.incidentType,
        attackerIp = IpAddress(this.attackerIp),
        
        targetUri = this.targetUri ?: this.payloadDump?.get("uri") ?: "UNKNOWN",
        
        confidenceScore = this.confidenceScore,

        actionTaken = this.actionTaken ?: "BLOCK",
    
        
        payloadDump = this.payloadDump ?: emptyMap()
)

fun Incident.toDto(): IncidentResponseDto {
    return IncidentResponseDto(
        incidentType = incidentType,
        attackerIp = attackerIp.value,
        targetUri = targetUri,
        actionTaken = actionTaken,
        confidenceScore = confidenceScore,
        timestamp = timestamp ?: Instant.now()
    )
}

data class NameValueDto(
    val name: String,
    val value: Long
)

data class IncidentStatsSummary(
    val total: Long,
    val mlBlocked: Long,
    val staticBlocked: Long,
    val allowed: Long
)

data class IncidentStatsDto(
    val total: Long,
    val mlBlocked: Long,
    val staticBlocked: Long,
    val allowed: Long,
    val attackDistribution: List<NameValueDto>,
    val topBlockedIps: List<NameValueDto>,
    val actionMetrics: List<NameValueDto>
)