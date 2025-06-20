package jku.multimediasysteme.persistence.config

import jku.multimediasysteme.shared.auth.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

/**
 * Security configuration for the application.
 * Enables stateless JWT-based authentication.
 */
@Configuration
@EnableWebSecurity
class SecurityConfig(private val jwtAuthenticationFilter: JwtAuthenticationFilter) {

    /**
     * Defines the security filter chain with JWT authentication and stateless session policy.
     *
     * @param http the HTTP security configuration
     * @return the configured security filter chain
     */
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() } // Disable CSRF protection for API
            .authorizeHttpRequests {
                it.requestMatchers("/auth/**").permitAll() // Public access to auth endpoints
                    .anyRequest().authenticated() // All other endpoints require auth
            }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) } // No HTTP session
            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter::class.java
            ) // Add JWT filter
            .build()
    }

    /**
     * Provides a password encoder using BCrypt hashing.
     *
     * @return the password encoder bean
     */
    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()
}