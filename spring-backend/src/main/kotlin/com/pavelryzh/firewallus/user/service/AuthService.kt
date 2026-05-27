package com.pavelryzh.firewallus.user.service

import com.pavelryzh.firewallus.infra.security.JwtTokenService
import com.pavelryzh.firewallus.user.ports.AdminRepository
import com.pavelryzh.firewallus.user.api.LoginDto
import com.pavelryzh.firewallus.user.event.AdminLoginEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.stereotype.Service
import org.springframework.security.crypto.password.PasswordEncoder


@Service
class AuthService(
    private val adminRepository: AdminRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenService: JwtTokenService,
    private val eventPublisher: ApplicationEventPublisher
) {

    fun authenticate(loginDto: LoginDto): String {
        val admin = adminRepository.findByUsername(loginDto.username)
            ?: throw BadCredentialsException("Неверный логин или пароль")

        if (!passwordEncoder.matches(loginDto.password, admin.passwordHash)) {
            throw BadCredentialsException("Неверный логин или пароль")
        }

        eventPublisher.publishEvent(AdminLoginEvent(admin.id!!, admin.username))

        return jwtTokenService.generateToken(admin)
    }
}