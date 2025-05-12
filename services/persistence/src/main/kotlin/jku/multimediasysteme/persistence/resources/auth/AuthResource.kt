package jku.multimediasysteme.persistence.resources.auth

import jku.multimediasysteme.persistence.dto.AuthRequest
import jku.multimediasysteme.persistence.service.AuthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST controller for user authentication.
 * Provides endpoints for registration, login, and user info retrieval.
 */
@RestController
@RequestMapping("/auth")
class AuthResource(
    private val authService: AuthService
) {
    /**
     * Registers a new user with the given username and password.
     *
     * @param request the registration data (username, password)
     * @return a JWT token for the newly created user
     */
    @PostMapping("/register")
    fun register(@RequestBody request: AuthRequest): ResponseEntity<String> {
        return ResponseEntity.ok(authService.register(request))
    }

    /**
     * Logs in a user by validating credentials and issuing a JWT.
     *
     * @param request the login data (username, password)
     * @return a JWT token if login is successful
     */
    @PostMapping("/login")
    fun login(@RequestBody request: AuthRequest): ResponseEntity<String> {
        return ResponseEntity.ok(authService.login(request))
    }

    /**
     * Returns information about the currently authenticated user.
     * Expects a valid JWT in the Authorization header.
     *
     * @param authHeader the HTTP Authorization header (must start with "Bearer ")
     * @return user info (id, username) or an error message
     */
    @GetMapping("/me")
    fun me(@RequestHeader("Authorization") authHeader: String): ResponseEntity<Any> {
        // Validate that the header starts with "Bearer "
        if (!authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(mapOf("error" to "Ungültiger Authorization-Header"))
        }

        val token = authHeader.removePrefix("Bearer ").trim()

        return try {
            // Extract and return user information from the token
            val user = authService.getUserFromToken(token)
            ResponseEntity.ok(
                mapOf(
                    "id" to user.id,
                    "username" to user.username
                )
            )
        } catch (e: Exception) {
            // Return 401 if the token is invalid or user not found
            ResponseEntity.status(401).body(mapOf("error" to e.message))
        }
    }
}