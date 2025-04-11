package jku.multimediasysteme.shared.jpa.transcription.model

import jakarta.persistence.*
import jku.multimediasysteme.shared.jpa.AbstractUserEntity
import java.util.*

@Entity
@Table(name = "smart_sessions")
data class SmartSession(
    @Id
    var id: UUID,

    override var userId: UUID,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transcription_id", nullable = true)
    var transcription: Transcription? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "summary_id", nullable = true)
    var summary: Summary? = null,

    val createdAt: Long = System.currentTimeMillis()
) : AbstractUserEntity(userId)