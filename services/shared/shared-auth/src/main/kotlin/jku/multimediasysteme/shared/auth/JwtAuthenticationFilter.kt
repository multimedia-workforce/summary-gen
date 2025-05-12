package jku.multimediasysteme.shared.auth

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * This filter is executed once per HTTP request.
 * It checks whether a valid JWT is present in the Authorization header
 * and sets the corresponding user authentication in the SecurityContext.
 */
@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // Read the Authorization header from the HTTP request
        val authHeader = request.getHeader("Authorization") ?: return filterChain.doFilter(request, response)

        // If the header doesn't start with "Bearer ", skip JWT processing
        if (!authHeader.startsWith("Bearer ")) {
            return filterChain.doFilter(request, response)
        }

        // Extract the JWT by removing the "Bearer " prefix
        val token = authHeader.removePrefix("Bearer ")

        // Extract the user ID from the token
        val userId = jwtService.extractUserId(token)

        // If no authentication is yet stored in the SecurityContext, set it
        if (SecurityContextHolder.getContext().authentication == null) {
            // Create an authentication token using the user ID (no credentials, no authorities)
            val authentication = UsernamePasswordAuthenticationToken(userId, null, emptyList())

            // Attach request-specific details (IP, session info, etc.)
            authentication.details = WebAuthenticationDetailsSource().buildDetails(request)

            // Set the authentication in the SecurityContext
            SecurityContextHolder.getContext().authentication = authentication
        }

        // Continue with the remaining filter chain
        filterChain.doFilter(request, response)
    }
}