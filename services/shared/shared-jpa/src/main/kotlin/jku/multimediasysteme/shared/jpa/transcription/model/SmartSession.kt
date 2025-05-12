package jku.multimediasysteme.shared.jpa.transcription.model

import jakarta.persistence.*
import jku.multimediasysteme.shared.jpa.AbstractUserEntity
import java.util.*

/**
 * Entity representing a session that links a transcription and its corresponding summary
 * for a specific user.
 */
@Entity
@Table(name = "smart_sessions")
data class SmartSession(

    // Unique identifier for the session (UUID generated automatically)
    @Id
    var id: UUID = UUID.randomUUID(),

    // Inherited user ID from abstract base entity (used for ownership)
    override var userId: UUID,

    // One-to-one relationship to a transcription (lazy-loaded)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transcription_id", nullable = true)
    var transcription: Transcription? = null,

    // One-to-one relationship to a summary (lazy-loaded)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "summary_id", nullable = true)
    var summary: Summary? = null,

    // Timestamp for when the session was created (milliseconds since epoch)
    val createdAt: Long = System.currentTimeMillis()

// Inherit base class to attach user context
) : AbstractUserEntity(userId)