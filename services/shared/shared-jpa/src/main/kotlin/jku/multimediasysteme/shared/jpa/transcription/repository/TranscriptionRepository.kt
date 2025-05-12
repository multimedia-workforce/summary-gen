package jku.multimediasysteme.shared.jpa.transcription.repository

import jku.multimediasysteme.shared.jpa.transcription.model.Transcription
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

/**
 * Repository for managing Transcription entities.
 * Includes user-specific query methods.
 */
@Repository
interface TranscriptionRepository : JpaRepository<Transcription, UUID> {
    /**
     * Finds all transcriptions belonging to the specified user.
     *
     * @param userId the UUID of the user
     * @return a list of the user's transcriptions
     */
    fun findAllByUserId(userId: UUID): List<Transcription>

    /**
     * Finds a transcription by ID only if it belongs to the specified user.
     *
     * @param id the ID of the transcription
     * @param userId the UUID of the user
     * @return the transcription if it exists and belongs to the user, otherwise null
     */
    fun findByIdAndUserId(id: UUID, userId: UUID): Transcription?
}