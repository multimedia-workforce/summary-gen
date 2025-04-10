package jku.multimediasysteme.persistanceservice.resources

import jku.multimediasysteme.persistanceservice.data.transcription.model.Transcription
import jku.multimediasysteme.persistanceservice.data.transcription.repository.TranscriptionRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/transcriptions")
class TranscriptionResource(
    private val repository: TranscriptionRepository
) {

    @PostMapping
    fun create(@RequestBody transcription: Transcription): Transcription =
        repository.save(transcription)

    @GetMapping
    fun getByUser(@RequestParam userId: UUID): List<Transcription> =
        repository.findAllByUserId(userId)

    @GetMapping("/{id}")
    fun getById(
        @PathVariable id: UUID,
        @RequestParam userId: UUID
    ): ResponseEntity<Transcription> {
        val transcription = repository.findByIdAndUserId(id, userId)
        return transcription?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }

    @DeleteMapping("/{id}")
    fun deleteById(
        @PathVariable id: UUID,
        @RequestParam userId: UUID
    ): ResponseEntity<Void> {
        val transcription = repository.findByIdAndUserId(id, userId)
        return if (transcription != null) {
            repository.delete(transcription)
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}