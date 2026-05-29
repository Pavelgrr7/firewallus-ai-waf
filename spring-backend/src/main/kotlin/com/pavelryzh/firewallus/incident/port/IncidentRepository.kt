package com.pavelryzh.firewallus.incident.port

import com.pavelryzh.firewallus.incident.api.IncidentStatsSummary
import com.pavelryzh.firewallus.incident.domain.Incident
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface IncidentRepository: JpaRepository<Incident, UUID> {

    @Query("""
        SELECT new com.pavelryzh.firewallus.incident.api.IncidentStatsSummary(
            COUNT(i),
            COALESCE(SUM(CASE WHEN i.actionTaken = 'BLOCK' AND i.confidenceScore IS NOT NULL THEN 1L ELSE 0L END), 0L),
            COALESCE(SUM(CASE WHEN i.actionTaken = 'BLOCK' AND i.confidenceScore IS NULL THEN 1L ELSE 0L END), 0L),
            COALESCE(SUM(CASE WHEN i.actionTaken = 'ALLOW' OR i.actionTaken = 'LOG' THEN 1L ELSE 0L END), 0L)
        )
        FROM Incident i
    """)
    fun getIncidentStats(): IncidentStatsSummary

    @Query("""
        SELECT i.incidentType, COUNT(i)
        FROM Incident i
        GROUP BY i.incidentType
    """)
    fun getAttackDistribution(): List<Array<Any>>

    @Query("""
        SELECT i.actionTaken, COUNT(i)
        FROM Incident i
        GROUP BY i.actionTaken
    """)
    fun getActionMetrics(): List<Array<Any>>

    @Query("""
        SELECT i.attackerIp, COUNT(i)
        FROM Incident i
        WHERE i.actionTaken = 'BLOCK'
        GROUP BY i.attackerIp
        ORDER BY COUNT(i) DESC
    """)
    fun getTopBlockedIps(pageable: Pageable): List<Array<Any>>
}