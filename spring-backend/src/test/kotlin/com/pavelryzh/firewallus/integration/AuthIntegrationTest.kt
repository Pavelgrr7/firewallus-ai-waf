package com.pavelryzh.firewallus.integration

import com.pavelryzh.firewallus.infra.security.JwtTokenService
import com.pavelryzh.firewallus.user.api.LoginDto
import com.pavelryzh.firewallus.user.api.TokenResponseDto
import com.pavelryzh.firewallus.user.domain.Admin
import com.pavelryzh.firewallus.user.ports.AdminRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import tools.jackson.databind.ObjectMapper

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AuthIntegrationTest {
    companion object {
        @Container
        @ServiceConnection
        val postgres = PostgreSQLContainer("postgres:17-alpine")
    }

    @Autowired lateinit var mockMvc: MockMvc

    @Autowired lateinit var adminRepository: AdminRepository

    @Autowired lateinit var passwordEncoder: PasswordEncoder

    @Autowired lateinit var jwtTokenService: JwtTokenService

    @Autowired lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun cleanUp() {
        adminRepository.deleteAll()
    }

    @Test
    fun `POST login with correct credentials should return valid JWT`() {
        val rawPassword = "mySecretPassword123"
        val username = "super_admin"
        val realAdmin =
            Admin(
                username = username,
                passwordHash = passwordEncoder.encodeSafe(rawPassword),
            )
        adminRepository.save(realAdmin)

        val loginPayload = LoginDto(username = username, password = rawPassword)

        val mvcResult =
            mockMvc
                .perform(
                    post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginPayload)),
                ).andExpect(status().isOk)
                .andReturn()

        val responseJson = mvcResult.response.contentAsString
        val tokenResponse = objectMapper.readValue(responseJson, TokenResponseDto::class.java)

        assertNotNull(tokenResponse.token, "Token should not be null")
        assertTrue(tokenResponse.token.isNotBlank(), "Token should not be empty")

        assertTrue(jwtTokenService.validateToken(tokenResponse.token), "JWT signature must be valid")
        assertEquals(username, jwtTokenService.getUsernameFromToken(tokenResponse.token))
    }

    @Test
    fun `POST login with WRONG password should return 401 status code`() {
        val realAdmin =
            Admin(
                username = "victim_admin",
                passwordHash = passwordEncoder.encodeSafe("correct_pass"),
            )
        adminRepository.save(realAdmin)

        val wrongLoginPayload = LoginDto(username = "victim_admin", password = "WRONG_PASSWORD")

        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(wrongLoginPayload)),
            ).andExpect(status().isUnauthorized)
    }

    fun PasswordEncoder.encodeSafe(rawPassword: String): String =
        passwordEncoder.encode(rawPassword) ?: throw RuntimeException("passwordEncoder were unable to encode password: $rawPassword")
}
