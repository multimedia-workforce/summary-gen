package jku.multimediasysteme.persistence.resources.auth

import jku.multimediasysteme.persistence.dto.AuthRequest
import jku.multimediasysteme.persistence.service.AuthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthResource(
    private val authService: AuthService
) {
    @PostMapping("/register")
    fun register(@RequestBody request: AuthRequest): ResponseEntity<String> {
        return ResponseEntity.ok(authService.register(request))
    }

    @PostMapping("/login")
    fun login(@RequestBody request: AuthRequest): ResponseEntity<String> {
        return ResponseEntity.ok(authService.login(request))
    }

    @GetMapping("/me")
    fun me(@RequestHeader("Authorization") authHeader: String): ResponseEntity<Any> {
        if (!authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(mapOf("error" to "Ungültiger Authorization-Header"))
        }

        val token = authHeader.removePrefix("Bearer ").trim()

        return try {
            val user = authService.getUserFromToken(token)
            ResponseEntity.ok(mapOf(
                "id" to user.id,
                "username" to user.username
            ))
        } catch (e: Exception) {
            ResponseEntity.status(401).body(mapOf("error" to e.message))
        }
    }

}