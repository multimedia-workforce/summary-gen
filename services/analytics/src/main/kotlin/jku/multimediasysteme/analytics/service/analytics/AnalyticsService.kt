package jku.multimediasysteme.analytics.service.analytics

import jku.multimediasysteme.analytics.data.analytics.ActivityStats
import jku.multimediasysteme.analytics.data.analytics.HeatmapCell
import jku.multimediasysteme.analytics.data.analytics.SummaryLengthStats
import jku.multimediasysteme.analytics.data.analytics.TranscriptionMetrics
import jku.multimediasysteme.shared.jpa.transcription.model.Transcription
import jku.multimediasysteme.shared.jpa.transcription.repository.TranscriptionRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneId
import java.util.*

@Service
class AnalyticsService(private val transcriptionRepository: TranscriptionRepository) {
    fun getTranscriptionMetrics(userId: UUID): TranscriptionMetrics {
        val transcriptions = transcriptionRepository.findAllByUserId(userId)
        return buildMetrics(transcriptions)
    }

    fun getTranscriptionMetrics(userId: UUID, ids: List<UUID>): TranscriptionMetrics {
        val transcriptions = transcriptionRepository.findAllById(ids).filter { it.userId == userId }
        return buildMetrics(transcriptions)
    }

    private fun buildMetrics(transcriptions: List<Transcription>): TranscriptionMetrics {
        return TranscriptionMetrics(
            totalTranscriptions = transcriptions.size,
            summaryStats = buildSummaryStats(transcriptions),
            //transcriptionTimeStats = buildTranscriptionTimeStats(transcriptions),
            activityStats = buildActivityStats(transcriptions),
            dailyHeatmap = buildHeatmap(transcriptions)
        )
    }

    private fun buildSummaryStats(transcriptions: List<Transcription>): SummaryLengthStats {
        val lengths = transcriptions.map { it.summaryText?.length ?: 0 }
        return SummaryLengthStats(
            averageLength = lengths.average(),
            maxLength = lengths.maxOrNull(),
            minLength = lengths.minOrNull()
        )
    }

//    private fun buildTranscriptionTimeStats(transcriptions: List<Transcription>): TranscriptionTimeStats {
//        val times = transcriptions.mapNotNull { it.transcriptionTime }.sorted()
//        return TranscriptionTimeStats(
//            average = times.average(),
//            median = times.getOrNull(times.size / 2),
//            max = times.maxOrNull(),
//            min = times.minOrNull()
//        )
//    }

    private fun buildActivityStats(transcriptions: List<Transcription>): ActivityStats {
        val zone = ZoneId.systemDefault()
        val groupedByDay =
            transcriptions.groupingBy { Instant.ofEpochMilli(it.createdAt).atZone(zone).toLocalDate() }.eachCount()

        return ActivityStats(
            firstCreatedAt = transcriptions.minByOrNull { it.createdAt }?.createdAt,
            lastCreatedAt = transcriptions.maxByOrNull { it.createdAt }?.createdAt,
            mostActiveWeekday = transcriptions
                .groupingBy { Instant.ofEpochMilli(it.createdAt).atZone(zone).dayOfWeek }
                .eachCount()
                .maxByOrNull { it.value }?.key?.name,
            avgPerDay = groupedByDay.values.average()
        )
    }

    private fun buildHeatmap(transcriptions: List<Transcription>): List<HeatmapCell> {
        val zone = ZoneId.systemDefault()
        return transcriptions.groupingBy { Instant.ofEpochMilli(it.createdAt).atZone(zone).toLocalDate() }.eachCount()
            .map { (date, count) -> HeatmapCell(date.toString(), count) }
    }
}