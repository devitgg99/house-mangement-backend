package com.example.pvhcenima_api.service

import com.example.pvhcenima_api.config.JwtProperties
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.util.*

@Service
class TokenService(
    jwtProperties: JwtProperties,
) {
    private val secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256)

    // Generate token with user ID stored in claims
    fun generate(
        userDetails: UserDetails,
        userId: UUID,
        expirationDate: Date,
        additionalClaims: Map<String, Any> = emptyMap()
    ): String {
        val claims = additionalClaims.toMutableMap()
        claims["userId"] = userId.toString()  // Store userId in token
        
        return Jwts.builder()
            .setSubject(userDetails.username)
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(expirationDate)
            .addClaims(claims)
            .signWith(secretKey)
            .compact()
    }

    // Extract username from token
    fun extractUsername(token: String): String =
        getAllClaims(token).subject

    // Extract userId from token
    fun extractUserId(token: String): UUID =
        UUID.fromString(getAllClaims(token)["userId"] as String)

    // Check if token is expired
    fun isExpired(token: String): Boolean =
        getAllClaims(token).expiration.before(Date(System.currentTimeMillis()))

    // Validate token
    fun isValid(token: String, userDetails: UserDetails): Boolean {
        val userName = extractUsername(token)
        return userName == userDetails.username && !isExpired(token)
    }

    // Get all claims from token
    private fun getAllClaims(token: String): Claims {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .body
        } catch (ex: Exception) {
            throw IllegalArgumentException("Invalid JWT token", ex)
        }
    }
}