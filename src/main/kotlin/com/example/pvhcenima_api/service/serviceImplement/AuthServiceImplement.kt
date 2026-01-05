package com.example.pvhcenima_api.service.serviceImplement

import com.example.pvhcenima_api.config.JwtProperties
import com.example.pvhcenima_api.model.entity.Role
import com.example.pvhcenima_api.model.entity.User
import com.example.pvhcenima_api.model.request.UserLogin
import com.example.pvhcenima_api.model.request.UserRequest
import com.example.pvhcenima_api.repository.UserRepository
import com.example.pvhcenima_api.service.AuthService
import com.example.pvhcenima_api.service.CustomUserDetailService
import com.example.pvhcenima_api.service.TokenService
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.sql.Date

@Service
class AuthServiceImplement(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authManager: AuthenticationManager,
    private val userDetailsService: CustomUserDetailService,
    private val tokenService: TokenService,
    private val jwtProperties: JwtProperties
) : AuthService {
    
    override fun register(userRequest: UserRequest) {
        // Prevent self-registration as ADMIN
        if (userRequest.role == Role.ADMIN) {
            throw IllegalArgumentException("Cannot register as ADMIN")
        }
        
        // Check for duplicate email
        if (userRepository.existsByEmail(userRequest.email)) {
            throw IllegalArgumentException("Email already exists")
        }
        
        // Check for duplicate phone number
        if (userRepository.existsByPhoneNumber(userRequest.phoneNumber)) {
            throw IllegalArgumentException("Phone number already exists")
        }
        
        val user = User(
            fullName = userRequest.fullName,
            password = passwordEncoder.encode(userRequest.password)!!,
            email = userRequest.email,
            role = userRequest.role,  // Use role from request
            phoneNumber = userRequest.phoneNumber,
        )
        userRepository.save(user)
    }

    override fun login(userRequest: UserLogin): String {
        authManager.authenticate(
            UsernamePasswordAuthenticationToken(userRequest.emailOrPhonenumber, userRequest.password),
        )

        // Get the actual user entity to get userId
        val userEntity = userRepository.findByEmail(userRequest.emailOrPhonenumber)
            ?: userRepository.findByPhoneNumber(userRequest.emailOrPhonenumber)
            ?: throw IllegalArgumentException("User not found")

        val userDetails = userDetailsService.loadUserByUsername(userRequest.emailOrPhonenumber)

        val accessToken = tokenService.generate(
            userDetails = userDetails,
            userId = userEntity.userId!!,  // Include userId in token
            expirationDate = Date(System.currentTimeMillis() + jwtProperties.accessTokenExpiration)
        )
        return accessToken
    }
}