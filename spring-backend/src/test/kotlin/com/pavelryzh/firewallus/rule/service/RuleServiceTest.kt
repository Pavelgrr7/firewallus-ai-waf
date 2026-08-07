package com.pavelryzh.firewallus.rule.service

import com.pavelryzh.firewallus.rule.event.RuleCacheEvent
import com.pavelryzh.firewallus.rule.port.RuleRepository
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.springframework.context.ApplicationEventPublisher
import com.pavelryzh.firewallus.infra.security.CurrentAdminProvider
import com.pavelryzh.firewallus.rule.api.CreateRuleDto
import com.pavelryzh.firewallus.rule.api.UpdateRuleDto
import com.pavelryzh.firewallus.rule.domain.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.util.Optional
import java.util.UUID

class RuleServiceTest {

    @ExtendWith(MockitoExtension::class)
    class RuleServiceTest {

        @Mock
        lateinit var ruleRepo: RuleRepository
        @Mock
        lateinit var eventPublisher: ApplicationEventPublisher
        @Mock
        lateinit var currentAdminProvider: CurrentAdminProvider

        @InjectMocks
        lateinit var ruleService: RuleService

        @Captor
        lateinit var eventCaptor: ArgumentCaptor<RuleCacheEvent.Saved>

        // Создание правила - Happy Path
        @Test
        fun `createRule should save entity and publish Saved event`() {
            val dto = CreateRuleDto("Test Rule", Action.BLOCK, ConditionNode(), true)
            val adminId = UUID.randomUUID()

            whenever(currentAdminProvider.getCurrentAdminId()).thenReturn(adminId)
            whenever(ruleRepo.save(any())).thenAnswer { invocation ->
                val rule = invocation.arguments[0] as Rule
                rule.id = 52
                rule
            }

            val result = ruleService.createRule(dto)

            assertEquals("Test Rule", result.name)
            assertEquals(52, result.id)

            // Проверяем сайд-эффект: было ли отправлено событие в кэш?
            verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture())
            val publishedEvent = eventCaptor.value

            assertEquals(52, publishedEvent.ruleId)
            assertEquals(adminId, publishedEvent.adminId)
        }

        // Частичное обновление - Edge Case
        @Test
        fun `updateRule should only update non-null fields from DTO`() {
            val existingRule = Rule("Old Name", Action.LOG, ConditionNode(), true).apply { id = 1 }

            // DTO, в котором мы хотим поменять Action, остальное null
            val updateDto = UpdateRuleDto(name = null, action = Action.ALLOW, rootNode = null)

            whenever(ruleRepo.findById(1)).thenReturn(Optional.of(existingRule))
            whenever(currentAdminProvider.getCurrentAdminId()).thenReturn(UUID.randomUUID())

            val result = ruleService.updateRule(1, updateDto)

            assertEquals("Old Name", result.name)
            assertEquals(Action.ALLOW, result.action)
        }

        // Exception Path
        @Test
        fun `updateRule should throw RuleNotFoundException if rule does not exist`() {
            val wrongId = 999
            val updateDto = UpdateRuleDto(
                name = "Hacked",
                action = Action.ALLOW,
                rootNode = null)

            whenever(ruleRepo.findById(wrongId)).thenReturn(Optional.empty())

            val exception = assertThrows<RuleNotFoundException> {
                ruleService.updateRule(wrongId, updateDto)
            }

            assertEquals("Rule with id 999 not found.", exception.message)

            verify(eventPublisher, never()).publishEvent(any())
        }
    }
}