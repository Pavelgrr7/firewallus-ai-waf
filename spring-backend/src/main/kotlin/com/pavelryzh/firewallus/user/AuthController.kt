package main.kotlin.com.pavelryzh.firewallus.user

import com.pavelryzh.firewallus.user.LoginDto
import com.pavelryzh.firewallus.user.TokenResponseDto
import com.pavelryzh.firewallus.user.service.AuthService
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/login")
    fun login(@Valid @RequestBody loginDto: LoginDto): TokenResponseDto {
        val token = authService.authenticate(loginDto.username, loginDto.password)
        return TokenResponseDto(token)
    }
}