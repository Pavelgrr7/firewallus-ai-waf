package com.pavelryzh.firewallus.incident.api

import com.fasterxml.jackson.annotation.JsonProperty
import com.pavelryzh.firewallus.incident.domain.Incident
import com.pavelryzh.firewallus.rule.domain.IpAddress
import java.time.Instant

data class IncidentEventDto(
    val incidentType: String,
    val attackerIp: String,
    val targetUri: String,
    val actionTaken: String,
    val confidenceScore: Float? = null,

    @JsonProperty("headersDump")
    val payloadDump: Map<String, String>
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
    incidentType = incidentType,
    attackerIp = IpAddress(this.attackerIp),
    targetUri = targetUri,
    actionTaken = actionTaken,
    confidenceScore = confidenceScore,
    payloadDump = payloadDump,
)

fun Incident.toDto(): IncidentResponseDto {
    return IncidentResponseDto(
        incidentType = incidentType,
        attackerIp = attackerIp.value,
        targetUri = targetUri,
        actionTaken = actionTaken,
        confidenceScore = confidenceScore,
        timestamp = timestamp
    )
}