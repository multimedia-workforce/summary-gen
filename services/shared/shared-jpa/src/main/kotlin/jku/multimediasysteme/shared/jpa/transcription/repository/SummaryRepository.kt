package jku.multimediasysteme.shared.jpa.transcription.repository

import jku.multimediasysteme.shared.jpa.transcription.model.Summary
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface SummaryRepository : JpaRepository<Summary, UUID> {
    fun findAllByUserId(userId: UUID): List<Summary>
    fun findByIdAndUserId(id: UUID, userId: UUID): Summary?
}