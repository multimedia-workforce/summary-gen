package jku.multimediasysteme.persistence.resources.smartsession

import jku.multimediasysteme.shared.jpa.transcription.model.SmartSession
import jku.multimediasysteme.shared.jpa.transcription.repository.SmartSessionRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/smartSessions")
class SmartSessionResource(
    private val repository: SmartSessionRepository,
) {
    @GetMapping
    fun getByUser(@RequestParam userId: UUID): List<SmartSession> =
        repository.findAllByUserId(userId)

    @DeleteMapping("/{id}")
    fun deleteById(
        @PathVariable id: UUID,
        @RequestParam userId: UUID
    ): ResponseEntity<Void> {
        val session = repository.findByIdAndUserId(id, userId)
        return if (session.isPresent) {
            repository.delete(session.get())
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}