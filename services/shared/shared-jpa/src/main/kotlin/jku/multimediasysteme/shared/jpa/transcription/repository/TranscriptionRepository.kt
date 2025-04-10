package jku.multimediasysteme.shared.jpa.transcription.repository

import jku.multimediasysteme.shared.jpa.transcription.model.Transcription
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TranscriptionRepository : JpaRepository<Transcription, UUID> {
    fun findAllByUserId(userId: UUID): List<Transcription>
    fun findByIdAndUserId(id: UUID, userId: UUID): Transcription?
}