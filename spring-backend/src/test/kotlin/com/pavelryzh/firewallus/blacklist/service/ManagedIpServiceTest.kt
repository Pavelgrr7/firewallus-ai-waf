package com.pavelryzh.firewallus.blacklist.service
import com.pavelryzh.firewallus.blacklist.domain.IpListType
import com.pavelryzh.firewallus.blacklist.domain.ManagedIp
import com.pavelryzh.firewallus.blacklist.event.ManagedIpEvent
import com.pavelryzh.firewallus.blacklist.port.ManagedIpRepository
import com.pavelryzh.firewallus.infra.security.CurrentAdminProvider
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.context.ApplicationEventPublisher
import java.util.*

@ExtendWith(MockitoExtension::class)
class ManagedIpServiceTest {
    @Mock lateinit var ipRepo: ManagedIpRepository
    @Mock lateinit var eventPublisher: ApplicationEventPublisher
    @Mock lateinit var currentAdminProvider: CurrentAdminProvider

    @InjectMocks lateinit var managedIpService: ManagedIpService

    @Captor lateinit var eventCaptor: ArgumentCaptor<ManagedIpEvent.Added>

    @Test
    fun `addIp should save IP and publish event`() {
        val adminId = UUID.randomUUID()
        whenever(currentAdminProvider.getCurrentAdminId()).thenReturn(adminId)
        whenever(ipRepo.findByIpAddress(any())).thenReturn(null) // IP свободен
        whenever(ipRepo.save(any())).thenAnswer { it.arguments[0] as ManagedIp }

        val result = managedIpService.addIp("192.168.1.10", IpListType.BLACKLIST, "Spammer")

        assertEquals("192.168.1.10", result.ipAddress)
        assertEquals(IpListType.BLACKLIST, result.listType)

        verify(eventPublisher).publishEvent(eventCaptor.capture())
        assertEquals("192.168.1.10", eventCaptor.value.ipAddress)
        assertEquals(adminId, eventCaptor.value.adminId)
    }

    @Test
    fun `addIp should throw Exception if IP already exists`() {
        val existingIp = ManagedIp("10.0.0.1", IpListType.WHITELIST)
        whenever(ipRepo.findByIpAddress("10.0.0.1")).thenReturn(existingIp)

        val exception = assertThrows<IllegalArgumentException> {
            managedIpService.addIp("10.0.0.1", IpListType.BLACKLIST, null)
        }
        assertTrue(exception.message!!.contains("уже находится в WHITELIST"))
        verify(eventPublisher, never()).publishEvent(any()) // Событие не улетело
    }

    @Test
    fun `addIp should fail immediately if IP format is invalid`() {
        // Здесь мы даже не мокаем репозиторий, потому что код должен упасть
        // ДО обращения к БД на этапе `val validIp = IpAddress(...)`
        assertThrows<IllegalArgumentException> {
            managedIpService.addIp("invalid-ip-string", IpListType.BLACKLIST, null)
        }
    }
}