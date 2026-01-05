package com.example.pvhcenima_api.service

import com.example.pvhcenima_api.model.entity.User
import com.example.pvhcenima_api.repository.UserRepository
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Service
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.util.*

@Service
class CurrentUserService(
    private val tokenService: TokenService,
    private val userRepository: UserRepository
) {
    
    // Get current user's ID from JWT token
    fun getCurrentUserId(): UUID {
        val token = extractTokenFromRequest()
        return tokenService.extractUserId(token)
    }
    
    // Get current user entity from database
    fun getCurrentUser(): User {
        val userId = getCurrentUserId()
        return userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }
    }
    
    // Get current user's email/phone (username) from token
    fun getCurrentUsername(): String {
        val token = extractTokenFromRequest()
        return tokenService.extractUsername(token)
    }
    
    // Extract JWT token from current request
    private fun extractTokenFromRequest(): String {
        val request = (RequestContextHolder.getRequestAttributes() as ServletRequestAttributes).request
        val authHeader = request.getHeader("Authorization")
            ?: throw IllegalArgumentException("No Authorization header")
        
        if (!authHeader.startsWith("Bearer ")) {
            throw IllegalArgumentException("Invalid Authorization header")
        }
        
        return authHeader.substringAfter("Bearer ")
    }
}

