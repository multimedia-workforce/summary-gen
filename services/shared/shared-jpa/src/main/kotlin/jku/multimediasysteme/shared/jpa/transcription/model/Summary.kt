package jku.multimediasysteme.shared.jpa.transcription.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jku.multimediasysteme.shared.jpa.AbstractUserEntity
import java.util.*

@Entity
@Table(name = "summaries")
data class Summary(
    @Id
    var id: UUID,

    override var userId: UUID,

    @Column(columnDefinition = "TEXT")
    var text: String? = null,

    var createdAt: Long = System.currentTimeMillis(),

    var time: Long?
) : AbstractUserEntity(userId)