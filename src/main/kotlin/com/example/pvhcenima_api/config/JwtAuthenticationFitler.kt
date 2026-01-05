package com.example.pvhcenima_api.config

import com.example.pvhcenima_api.service.CustomUserDetailService
import com.example.pvhcenima_api.service.TokenService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFitler(
    private val userDetails: CustomUserDetailService,
    private val tokenService: TokenService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")

        // No token provided - let Spring Security handle it (will return 401)
        if (authHeader.doesNotContainBearerToken()) {
            filterChain.doFilter(request, response)
            return
        }

        try {
            val jwtToken = authHeader!!.extractTokenValue()
            val userName = tokenService.extractUsername(jwtToken)

            if (userName != null && SecurityContextHolder.getContext().authentication == null) {
                val foundUser = userDetails.loadUserByUsername(userName)

                if (tokenService.isValid(jwtToken, foundUser)) {
                    updateContext(foundUser, request)
                }
            }
            
            filterChain.doFilter(request, response)
            
        } catch (ex: Exception) {
            // Token is invalid or expired - return 401
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            
            val message = when {
                ex.cause?.message?.contains("expired", ignoreCase = true) == true -> 
                    "Token has expired"
                else -> 
                    "Invalid token"
            }
            response.writer.write("""{"success":false,"message":"$message"}""")
        }
    }

    private fun updateContext(foundUser: UserDetails, request: HttpServletRequest) {
        val authToken = UsernamePasswordAuthenticationToken(foundUser, null, foundUser.authorities)
        authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
        SecurityContextHolder.getContext().authentication = authToken
    }

    private fun String?.doesNotContainBearerToken(): Boolean =
        this == null || !this.startsWith("Bearer ")

    private fun String.extractTokenValue(): String =
        this.substringAfter("Bearer ")
}