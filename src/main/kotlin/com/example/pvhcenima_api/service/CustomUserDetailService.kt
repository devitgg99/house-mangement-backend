package com.example.pvhcenima_api.service

import com.example.pvhcenima_api.repository.UserRepository
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

typealias ApplicationUser = com.example.pvhcenima_api.model.entity.User

@Service
class CustomUserDetailService(private val userRepository: UserRepository) : UserDetailsService {

    override fun loadUserByUsername(emailOrPhoneNumber: String): UserDetails {
        // Try to find by email first, then by phone number
        val user = userRepository.findByEmail(emailOrPhoneNumber)
            ?: userRepository.findByPhoneNumber(emailOrPhoneNumber)
            ?: throw UsernameNotFoundException("Invalid credentials")
        return user.mapToUserDetails()
    }

    private fun ApplicationUser.mapToUserDetails(): UserDetails = User.builder()
        .username(this.email ?: this.phoneNumber!!)  // Use email or phone as username
        .password(this.password)
        .roles(this.role.name)
        .build()
}