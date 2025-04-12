package jku.multimediasysteme.shared.jpa.transcription.repository

import jku.multimediasysteme.shared.jpa.transcription.model.SmartSession
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface SmartSessionRepository : JpaRepository<SmartSession, UUID> {
    fun findAllByUserId(userId: UUID): List<SmartSession>
    fun findByIdAndUserId(id: UUID, userId: UUID): Optional<SmartSession>
    fun findFirstByUserIdAndTranscriptionId(userId: UUID, transcriptionId: UUID): Optional<SmartSession>
}