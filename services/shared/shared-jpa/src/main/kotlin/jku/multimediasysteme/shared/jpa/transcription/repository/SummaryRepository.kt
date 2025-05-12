package jku.multimediasysteme.shared.jpa.transcription.repository

import jku.multimediasysteme.shared.jpa.transcription.model.Summary
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

/**
 * Repository for accessing Summary entities in the database.
 * Provides basic CRUD operations and custom queries for user-specific access.
 */
@Repository
interface SummaryRepository : JpaRepository<Summary, UUID> {
    /**
     * Finds all summaries belonging to a specific user.
     *
     * @param userId The UUID of the user.
     * @return A list of summaries created by the user.
     */
    fun findAllByUserId(userId: UUID): List<Summary>

    /**
     * Retrieves a summary by its ID and user.
     *
     * @param id The ID of the summary.
     * @param userId The UUID of the user who must own the summary.
     * @return The summary if it exists and is owned by the user; otherwise, null.
     */
    fun findByIdAndUserId(id: UUID, userId: UUID): Summary?
}