package jku.multimediasysteme.persistence.data.transcription.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jku.multimediasysteme.persistence.data.AbstractUserEntity
import java.util.*

@Entity
@Table(name = "transcriptions")
data class Transcription(
    @Id
    var id: UUID,

    override var userId: UUID,

    @Column(columnDefinition = "TEXT")
    var summaryText: String? = null,

    var createdAt: Long = System.currentTimeMillis()
) : AbstractUserEntity(userId)