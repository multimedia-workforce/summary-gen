package jku.multimediasysteme.shared.jpa.transcription.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jku.multimediasysteme.shared.jpa.AbstractUserEntity
import java.util.*

/**
 * Represents a speech-to-text transcription linked to a user.
 */
@Entity
@Table(name = "transcriptions")
data class Transcription(
    // Unique identifier for the transcription
    @Id
    var id: UUID,

    // ID of the user who owns this transcription
    override var userId: UUID,

    // The transcribed text content (stored as large text)
    @Column(columnDefinition = "TEXT")
    var text: String? = null,

    // Timestamp of when the transcription was created (in milliseconds)
    var createdAt: Long = System.currentTimeMillis(),

    // Duration of the original audio/video content in milliseconds (optional)
    var time: Long?
) : AbstractUserEntity(userId)