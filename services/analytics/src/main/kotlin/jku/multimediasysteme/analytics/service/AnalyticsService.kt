package jku.multimediasysteme.analytics.service

import jku.multimediasysteme.analytics.data.TranscriptionMetrics
import jku.multimediasysteme.shared.jpa.transcription.model.Transcription
import jku.multimediasysteme.shared.jpa.transcription.repository.TranscriptionRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class AnalyticsService(private val transcriptionRepository: TranscriptionRepository) {

    fun getTranscriptionMetrics(userId: UUID): TranscriptionMetrics {
        val transcriptions = transcriptionRepository.findAllByUserId(userId)
        return buildMetrics(transcriptions)
    }

    fun getTranscriptionMetrics(userId: UUID, ids: List<UUID>): TranscriptionMetrics {
        val selected = transcriptionRepository.findAllById(ids)
            .filter { it.userId == userId }
        return buildMetrics(selected)
    }

    private fun buildMetrics(transcriptions: List<Transcription>): TranscriptionMetrics {
        val total = transcriptions.size
        val avgSummary = if (total > 0) transcriptions.map { it.summaryText?.length ?: 0 }.average() else 0.0
        //  val avgTranscriptionTime = transcriptions.mapNotNull { it.transcriptionTime }.average()

        return TranscriptionMetrics(
            totalTranscriptions = total,
            averageSummaryLength = avgSummary,
            // avgTranscriptionTime = avgTranscriptionTime,
        )
    }
}