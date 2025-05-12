package jku.multimediasysteme.persistence.resources.smartsession

import jku.multimediasysteme.shared.jpa.transcription.model.SmartSession
import jku.multimediasysteme.shared.jpa.transcription.repository.SmartSessionRepository
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 * REST controller for managing SmartSession entities.
 *
 * SmartSessions serve as containers linking user-specific transcriptions and summaries.
 */
@RestController
@RequestMapping("/smartSessions")
class SmartSessionResource(
    private val repository: SmartSessionRepository,
) {
    /**
     * Returns all SmartSessions for the currently authenticated user.
     *
     * @param userId Injected user ID from JWT (via @AuthenticationPrincipal)
     * @return List of SmartSession entities
     */
    @GetMapping
    fun getByUser(@AuthenticationPrincipal userId: String): List<SmartSession> =
        repository.findAllByUserId(UUID.fromString(userId))

    /**
     * Retrieves a specific SmartSession by ID for the current user.
     *
     * @param userId the authenticated user ID
     * @param id the ID of the SmartSession to retrieve
     * @return SmartSession if found, or 404 Not Found
     */
    @GetMapping("/{id}")
    fun getByUserId(
        @AuthenticationPrincipal userId: String,
        @PathVariable id: UUID,
    ): ResponseEntity<SmartSession> {
        val session = repository.findByIdAndUserId(id, UUID.fromString(userId))
        return if (session.isPresent) {
            ResponseEntity.ok(session.get())
        } else {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * Deletes a SmartSession by ID if it belongs to the current user.
     *
     * @param userId the authenticated user ID
     * @param id the SmartSession ID to delete
     * @return HTTP 204 No Content if deleted, or 404 Not Found
     */
    @DeleteMapping("/{id}")
    fun deleteById(
        @AuthenticationPrincipal userId: String,
        @PathVariable id: UUID,
    ): ResponseEntity<Void> {
        val session = repository.findByIdAndUserId(id, UUID.fromString(userId))
        return if (session.isPresent) {
            repository.delete(session.get())
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}