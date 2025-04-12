package jku.multimediasysteme.persistence.service

import jku.multimediasysteme.persistence.dto.AuthRequest
import jku.multimediasysteme.persistence.entity.AppUser
import jku.multimediasysteme.persistence.repository.UserRepository
import jku.multimediasysteme.shared.auth.JwtService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.*

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
    private val passwordEncoder: PasswordEncoder
) {
    fun register(request: AuthRequest): String {
        if (userRepository.findByUsername(request.username) != null) {
            throw Exception("Benutzer existiert bereits.")
        }

        val encodedPassword = passwordEncoder.encode(request.password)
        val appUser = userRepository.save(AppUser(username = request.username, password = encodedPassword))

        return jwtService.generateToken(appUser.id)
    }

    fun login(request: AuthRequest): String {
        val user = userRepository.findByUsername(request.username)
            ?: throw Exception("Ungültige Anmeldedaten")

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw Exception("Ungültige Anmeldedaten")
        }

        return jwtService.generateToken(user.id)
    }

    fun getUserFromToken(token: String): AppUser {
        val userId = jwtService.extractUserId(token)
        return userRepository.findById(UUID.fromString(userId))
            .orElseThrow { Exception("Benutzer nicht gefunden.") }
    }

}