package com.pavelryzh.firewallus.incident.port

import com.pavelryzh.firewallus.incident.domain.Incident
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface IncidentRepository: JpaRepository<Incident, UUID> {
}