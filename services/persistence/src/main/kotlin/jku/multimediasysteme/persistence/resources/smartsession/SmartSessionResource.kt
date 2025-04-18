package jku.multimediasysteme.persistence.resources.smartsession

import jku.multimediasysteme.shared.jpa.transcription.model.SmartSession
import jku.multimediasysteme.shared.jpa.transcription.repository.SmartSessionRepository
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/smartSessions")
class SmartSessionResource(
    private val repository: SmartSessionRepository,
) {
    @GetMapping
    fun getByUser(@AuthenticationPrincipal userId: String): List<SmartSession> =
        repository.findAllByUserId(UUID.fromString(userId))

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