package jku.multimediasysteme.analytics.config

import jku.multimediasysteme.shared.auth.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
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
            .csrf { it.disable() }  // Disable CSRF protection for API
            .authorizeHttpRequests { it.anyRequest().authenticated() } // Require authentication for all endpoints
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // Don't store sessions on server
            }
            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter::class.java
            ) // Insert JWT validation before Spring's default auth filter
            .build()
    }
}