package com.example.pvhcenima_api.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.DefaultSecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfiguration(
    private val authenticationProvider: AuthenticationProvider,
    private val customAuthEntryPoint: CustomAuthEntryPoint,
    private val customAccessDeniedHandler: CustomAccessDeniedHandler,
) {
    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        jwtAuthenticationFitler: JwtAuthenticationFitler
    ): DefaultSecurityFilterChain = http
        .csrf { it.disable() }
        .formLogin { it.disable() }
        .cors { it.configurationSource(corsConfigurationSource()) }
        .authorizeHttpRequests {
            it.requestMatchers(
                "/api/v1/auth/**", "/api/v1/refresh", "/error", "/api/v1/file/**",
                "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/webjars/**"
            ).permitAll()
                .requestMatchers("/api/v1/house/**").hasRole("HOUSEOWNER")
                .anyRequest().fullyAuthenticated()
        }
        .exceptionHandling {
            it.authenticationEntryPoint(customAuthEntryPoint)      // 401 - No token
            it.accessDeniedHandler(customAccessDeniedHandler)      // 403 - Wrong role
        }
        .sessionManagement {
            it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        }
        .authenticationProvider(authenticationProvider)
        .addFilterBefore(jwtAuthenticationFitler, UsernamePasswordAuthenticationFilter::class.java)
        .build()

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration().apply {
            // Allow these origins (add your frontend URLs)
            allowedOrigins = listOf(
                "http://localhost:3000",    // React dev
                "http://localhost:5173",    // Vite dev
                "http://localhost:8081", // Other frontend
                "http://192.168.0.250:3000"
                // "https://your-production-domain.com"  // Production
            )
            // Or allow all origins (less secure, use for development only)
            // allowedOriginPatterns = listOf("*")

            allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            allowedHeaders = listOf("*")
            exposedHeaders = listOf("Authorization", "Content-Type")
            allowCredentials = true
            maxAge = 3600L  // Cache preflight for 1 hour
        }

        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", config)
        }
    }
}