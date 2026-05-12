package com.pavelryzh.firewallus.user

import jakarta.validation.constraints.NotBlank

data class LoginDto(
    @field:NotBlank val username: String,
    @field:NotBlank val password: String
)

data class TokenResponseDto(
    val token: String
)