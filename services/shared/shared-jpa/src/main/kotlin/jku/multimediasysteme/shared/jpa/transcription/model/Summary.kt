package jku.multimediasysteme.shared.jpa.transcription.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jku.multimediasysteme.shared.jpa.AbstractUserEntity
import java.util.*

/**
 * Represents a summary associated with a user's transcription session.
 */
@Entity
@Table(name = "summaries")
data class Summary(

    // Unique identifier for the summary
    @Id
    var id: UUID,

    // The user this summary belongs to (inherited from AbstractUserEntity)
    override var userId: UUID,

    // The actual summary text, stored as large text in the database
    @Column(columnDefinition = "TEXT")
    var text: String? = null,

    // Timestamp of when the summary was created (in milliseconds since epoch)
    var createdAt: Long = System.currentTimeMillis(),

    // Duration or processing time associated with the summary (optional)
    var time: Long?
) : AbstractUserEntity(userId)