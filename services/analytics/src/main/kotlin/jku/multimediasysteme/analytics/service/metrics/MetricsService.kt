package jku.multimediasysteme.analytics.service.metrics

import jku.multimediasysteme.analytics.data.metrics.SmartSessionMetrics
import jku.multimediasysteme.analytics.data.metrics.SummaryMetrics
import jku.multimediasysteme.analytics.data.metrics.TranscriptionMetrics
import jku.multimediasysteme.analytics.data.metrics.heatmap.HeatmapCell
import jku.multimediasysteme.analytics.data.metrics.stats.ActivityStats
import jku.multimediasysteme.analytics.data.metrics.stats.CreateTimeStats
import jku.multimediasysteme.analytics.data.metrics.stats.TextStats
import jku.multimediasysteme.shared.jpa.transcription.model.SmartSession
import jku.multimediasysteme.shared.jpa.transcription.model.Summary
import jku.multimediasysteme.shared.jpa.transcription.model.Transcription
import jku.multimediasysteme.shared.jpa.transcription.repository.SmartSessionRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneId
import java.util.*

@Service
class MetricsService(private val smartSessionRepository: SmartSessionRepository) {
    fun getSessionMetrics(userId: UUID): SmartSessionMetrics? {
        val sessions = smartSessionRepository.findAllByUserId(userId)
        if (sessions.isEmpty()) {
            return null
        }

        return buildSmartSessionMetrics(sessions)
    }

    fun getSessionMetrics(userId: UUID, ids: List<UUID>): SmartSessionMetrics? {
        val sessions = smartSessionRepository.findAllById(ids).filter { it.userId == userId }
        if (sessions.isEmpty()) {
            return null
        }

        return buildSmartSessionMetrics(sessions)
    }

    private fun buildSmartSessionMetrics(sessions: List<SmartSession>): SmartSessionMetrics {
        val transcriptions = sessions.mapNotNull { it.transcription }
        val summaries = sessions.mapNotNull { it.summary }

        return SmartSessionMetrics(
            dailyHeatmap = buildHeatmap(sessions),
            transcriptionMetrics = buildTranscriptionMetrics(transcriptions),
            summaryMetrics = buildSummaryMetrics(summaries)
        )
    }

    private fun buildTranscriptionMetrics(transcriptions: List<Transcription>): TranscriptionMetrics {
        return TranscriptionMetrics(
            totalTranscriptions = transcriptions.size,
            textStats = buildTextStats(transcriptions.mapNotNull { it.text }),
            createTimeStats = buildCreateTimeStats(transcriptions.mapNotNull { it.time }),
            activityStats = buildActivityStats(transcriptions.map { it.createdAt })
        )
    }

    private fun buildSummaryMetrics(summaries: List<Summary>): SummaryMetrics {
        return SummaryMetrics(
            totalSummaries = summaries.size,
            textStats = buildTextStats(summaries.mapNotNull { it.text }),
            createTimeStats = buildCreateTimeStats(summaries.mapNotNull { it.time }),
            activityStats = buildActivityStats(summaries.map { it.createdAt })
        )
    }

    private fun buildTextStats(texts: List<String>): TextStats {
        val lengths = texts.map { it.length }
        return TextStats(
            averageLength = lengths.average(),
            maxLength = lengths.maxOrNull(),
            minLength = lengths.minOrNull()
        )
    }

    private fun buildCreateTimeStats(times: List<Long>): CreateTimeStats {
        val sorted = times.sorted()
        return CreateTimeStats(
            average = sorted.average(),
            median = sorted.getOrNull(sorted.size / 2),
            max = sorted.maxOrNull(),
            min = sorted.minOrNull()
        )
    }

    private fun buildActivityStats(timestamps: List<Long>): ActivityStats {
        val zone = ZoneId.systemDefault()
        val groupedByDay = timestamps.groupingBy { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }.eachCount()

        return ActivityStats(
            firstCreatedAt = timestamps.minOrNull(),
            lastCreatedAt = timestamps.maxOrNull(),
            mostActiveWeekday = timestamps
                .groupingBy { Instant.ofEpochMilli(it).atZone(zone).dayOfWeek }
                .eachCount()
                .maxByOrNull { it.value }?.key?.name,
            avgPerDay = groupedByDay.values.average()
        )
    }

    private fun buildHeatmap(sessions: List<SmartSession>): List<HeatmapCell> {
        val zone = ZoneId.systemDefault()
        return sessions.groupingBy { Instant.ofEpochMilli(it.createdAt).atZone(zone).toLocalDate() }.eachCount()
            .map { (date, count) -> HeatmapCell(date.toString(), count) }
    }
}