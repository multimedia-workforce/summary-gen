package jku.multimediasysteme.persistanceservice.data.transcription.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jku.multimediasysteme.persistanceservice.data.AbstractUserEntity
import java.util.*

@Entity
@Table(name = "transcriptions")
data class Transcription(
    @Id
    var id: UUID = UUID.randomUUID(),

    override var userId: UUID,

    var description: String = "",

    @Column(columnDefinition = "TEXT")
    var summaryText: String? = null,

    var createdAt: Long = System.currentTimeMillis()
) : AbstractUserEntity(userId)