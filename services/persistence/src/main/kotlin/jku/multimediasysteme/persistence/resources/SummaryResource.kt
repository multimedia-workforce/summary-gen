package jku.multimediasysteme.persistence.resources

import jku.multimediasysteme.shared.jpa.transcription.model.Summary
import jku.multimediasysteme.shared.jpa.transcription.repository.SummaryRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/summaries")
class SummaryResource(
    private val repository: SummaryRepository,
) {
    @PostMapping
    fun create(@RequestBody transcription: Summary): Summary =
        repository.save(transcription)

    @GetMapping
    fun getByUser(@RequestParam userId: UUID): List<Summary> =
        repository.findAllByUserId(userId)

    @GetMapping("/{id}")
    fun getById(
        @PathVariable id: UUID,
        @RequestParam userId: UUID
    ): ResponseEntity<Summary> {
        val summary = repository.findByIdAndUserId(id, userId)
        return summary?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }

    @DeleteMapping("/{id}")
    fun deleteById(
        @PathVariable id: UUID,
        @RequestParam userId: UUID
    ): ResponseEntity<Void> {
        val summary = repository.findByIdAndUserId(id, userId)
        return if (summary != null) {
            repository.delete(summary)
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}