package com.pavelryzh.firewallus.user

data class LoginDto(
    val username: String,
    val password: String
)

data class TokenResponseDto(
    val token: String
)