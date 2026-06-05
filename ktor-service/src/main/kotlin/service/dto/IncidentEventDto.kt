package com.pavelryzh.service.dto

import kotlinx.serialization.Serializable

@Serializable
data class IncidentEventDto(
    val incidentType: String,
    val attackerIp: String,
    val targetUri: String,
    val actionTaken: String,
    val payloadDump: Map<String, String>
) : KafkaEvent