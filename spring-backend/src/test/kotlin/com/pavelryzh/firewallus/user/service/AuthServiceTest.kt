package com.pavelryzh.firewallus.user.service;

import com.pavelryzh.firewallus.infra.security.JwtTokenService
import com.pavelryzh.firewallus.user.api.LoginDto
import com.pavelryzh.firewallus.user.domain.Admin
import com.pavelryzh.firewallus.user.event.AdminLoginEvent
import com.pavelryzh.firewallus.user.ports.AdminRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class AuthServiceTest {

    @Mock
    lateinit var adminRepository: AdminRepository

    @Mock
    lateinit var passwordEncoder: PasswordEncoder

    @Mock
    lateinit var jwtTokenService: JwtTokenService

    @Mock
    lateinit var eventPublisher: ApplicationEventPublisher

    val fakeHash = "bcrypt_hash_123"
    val fakeAdminId: UUID = UUID.randomUUID()
    val expectedToken = "jwt.token.abc"
    val username = "test_admin"
    val password = "test_password"
    val admin = Admin(username, fakeHash).apply {
        id = fakeAdminId
    }
    val login = LoginDto(username, password)

    @InjectMocks
    lateinit var authService: AuthService

    @Captor
    lateinit var eventCaptor: ArgumentCaptor<AdminLoginEvent>

    @Test
    fun `authenticate with valid credentials should return actual jwt token`() {

        whenever(passwordEncoder.matches(password, fakeHash)).thenReturn(true)
        whenever(jwtTokenService.generateToken(any())).thenReturn(expectedToken)
        whenever(adminRepository.findByUsername(any())).thenReturn(admin)

        val result = authService.authenticate(login)

        assertEquals(expectedToken, result)
        // Проверяем сайд-эффект: было ли отправлено событие в кэш?
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture())

        val publishedEvent = eventCaptor.value

        assertEquals(username, publishedEvent.username)
        assertEquals(fakeAdminId, publishedEvent.adminId)
    }

    @Test
    fun `authenticate should throw BCE when repository return null`() {

        whenever(adminRepository.findByUsername(any())).thenReturn(null)

        assertThrows<BadCredentialsException> {
            authService.authenticate(login)
        }

        // authenticate pipeline should interrupt
        verifyNoInteractions(passwordEncoder, jwtTokenService, eventPublisher)

    }

    @Test
    fun `authenticate should throw BCE when password is incorrect`() {

        whenever(adminRepository.findByUsername(any())).thenReturn(admin)
        whenever(passwordEncoder.matches(password, fakeHash)).thenReturn(false)

        assertThrows<BadCredentialsException> {
            authService.authenticate(login)
        }

        verifyNoInteractions(jwtTokenService, eventPublisher)

    }
}
