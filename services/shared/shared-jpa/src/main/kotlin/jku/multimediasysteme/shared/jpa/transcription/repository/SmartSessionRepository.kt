package jku.multimediasysteme.shared.jpa.transcription.repository

import jku.multimediasysteme.shared.jpa.transcription.model.SmartSession
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

/**
 * Repository interface for accessing SmartSession entities from the database.
 * Extends JpaRepository to provide CRUD operations.
 */
interface SmartSessionRepository : JpaRepository<SmartSession, UUID> {

    /**
     * Retrieves all SmartSessions belonging to a specific user.
     *
     * @param userId The UUID of the user.
     * @return A list of SmartSession objects owned by the user.
     */
    fun findAllByUserId(userId: UUID): List<SmartSession>

    /**
     * Retrieves a specific SmartSession by its ID, but only if it belongs to the given user.
     *
     * @param id The UUID of the SmartSession.
     * @param userId The UUID of the user who must own the session.
     * @return An Optional containing the SmartSession if found and owned by the user; otherwise empty.
     */
    fun findByIdAndUserId(id: UUID, userId: UUID): Optional<SmartSession>

    /**
     * Retrieves the first SmartSession for a given user and transcription.

     * @param userId The UUID of the user.
     * @param transcriptionId The UUID of the transcription linked to the session.
     * @return An Optional containing the matching SmartSession if found; otherwise empty.
     */
    fun findFirstByUserIdAndTranscriptionId(userId: UUID, transcriptionId: UUID): Optional<SmartSession>
}