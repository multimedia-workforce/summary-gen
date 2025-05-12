package jku.multimediasysteme.persistence.service

import jku.multimediasysteme.persistence.dto.AuthRequest
import jku.multimediasysteme.persistence.entity.AppUser
import jku.multimediasysteme.persistence.repository.UserRepository
import jku.multimediasysteme.shared.auth.JwtService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.*

/**
 * Service class for handling user authentication and registration.
 * Provides methods for registering users, logging in, and extracting users from JWTs.
 */
@Service
class AuthService(
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
    private val passwordEncoder: PasswordEncoder
) {
    /**
     * Registers a new user and returns a JWT token.
     * If the username already exists, an exception is thrown.
     *
     * @param request the authentication request containing username and password
     * @return a signed JWT token for the newly registered user
     * @throws Exception if user already exists
     */
    fun register(request: AuthRequest): String {
        // Check if the username is already taken
        if (userRepository.findByUsername(request.username) != null) {
            throw Exception("Benutzer existiert bereits.")
        }

        // Encrypt the password using BCrypt
        val encodedPassword = passwordEncoder.encode(request.password)

        // Save the user in the database
        val appUser = userRepository.save(AppUser(username = request.username, password = encodedPassword))

        // Generate a JWT for the newly registered user
        return jwtService.generateToken(appUser.id)
    }

    /**
     * Logs in a user by validating the credentials and returning a JWT.
     *
     * @param request contains the login credentials (username & password)
     * @return a JWT token for authenticated access
     * @throws Exception if credentials are invalid
     */
    fun login(request: AuthRequest): String {
        // Find the user by username
        val user = userRepository.findByUsername(request.username)
            ?: throw Exception("Ungültige Anmeldedaten")

        // Check if the provided password matches the hashed password
        if (!passwordEncoder.matches(request.password, user.password)) {
            throw Exception("Ungültige Anmeldedaten")
        }

        // Generate and return a JWT
        return jwtService.generateToken(user.id)
    }

    /**
     * Extracts a user from a valid JWT token.
     *
     * @param token a valid JWT
     * @return the corresponding AppUser from the database
     * @throws Exception if user cannot be found
     */
    fun getUserFromToken(token: String): AppUser {
        // Decode the user ID from the token
        val userId = jwtService.extractUserId(token)

        // Load the user from the database
        return userRepository.findById(UUID.fromString(userId))
            .orElseThrow { Exception("Benutzer nicht gefunden.") }
    }
}