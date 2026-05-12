package com.pavelryzh.firewallus.infra.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtFilter(
    private val jwtTokenService: JwtTokenService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")

        if (authHeader.isNullOrBlank() || !authHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response)
            return
        }

        // "Bearer "
        val token = authHeader.substring(7)

        try {
            if (jwtTokenService.validateToken(token)) {
                val username = jwtTokenService.getUsernameFromToken(token)

                val authorities = listOf(SimpleGrantedAuthority("ROLE_ADMIN"))

                val authentication = UsernamePasswordAuthenticationToken(username, null, authorities)

                SecurityContextHolder.getContext().authentication = authentication
            }
        } catch (e: Exception) {
            logger.error("JWT Authentication failed: ${e.message}")
        }

        filterChain.doFilter(request, response)
    }
}