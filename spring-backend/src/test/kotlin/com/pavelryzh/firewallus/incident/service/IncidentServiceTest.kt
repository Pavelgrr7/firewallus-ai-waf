package com.pavelryzh.firewallus.incident.service
import com.pavelryzh.firewallus.incident.api.IncidentStatsSummary
import com.pavelryzh.firewallus.incident.port.IncidentRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.context.ApplicationEventPublisher

@ExtendWith(MockitoExtension::class)
class IncidentServiceTest {
    @Mock lateinit var incidentRepo: IncidentRepository
    @Mock lateinit var eventPublisher: ApplicationEventPublisher
    @InjectMocks lateinit var incidentService: IncidentService

    @Test
    fun `getStats should correctly map native queries to DTOs`() {
        // Имитируем сырые данные, которые возвращает Postgres через Hibernate
        val mockAttackDist = listOf(
            arrayOf<Any>("SQL_INJECTION", 150L),
            arrayOf<Any>("XSS", 50L)
        )
        val mockActionMetrics = listOf(arrayOf<Any>("BLOCK", 100L)) // Добавили мок для Action Metrics
        val mockSummary = IncidentStatsSummary(720L, 20L, 300L, 400L)

        val mockTopIps = listOf(
            arrayOf<Any>("192.168.1.55", 300L)
        )

        whenever(incidentRepo.getIncidentStats()).thenReturn(mockSummary)
        whenever(incidentRepo.getAttackDistribution()).thenReturn(mockAttackDist)
        whenever(incidentRepo.getActionMetrics()).thenReturn(mockActionMetrics)
        whenever(incidentRepo.getTopBlockedIps(any())).thenReturn(mockTopIps)

        val stats = incidentService.getStats()

        assertEquals(2, stats.attackDistribution.size)
        assertEquals("SQL_INJECTION", stats.attackDistribution[0].name)
        assertEquals(150L, stats.attackDistribution[0].value)

        assertEquals(1, stats.topBlockedIps.size)
        assertEquals("192.168.1.55", stats.topBlockedIps[0].name)
        assertEquals(300L, stats.topBlockedIps[0].value)
    }
}