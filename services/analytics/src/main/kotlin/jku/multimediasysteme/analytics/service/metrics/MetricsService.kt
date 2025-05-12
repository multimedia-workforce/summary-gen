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

/**
 * Service for calculating aggregated statistics (metrics) from SmartSessions.
 * Provides insights like activity levels, text length stats, creation times and heatmaps.
 */
@Service
class MetricsService(private val smartSessionRepository: SmartSessionRepository) {

    /**
     * Computes metrics for all SmartSessions of a given user.
     *
     * @param userId the UUID of the user
     * @return computed SmartSessionMetrics, or null if no sessions found
     */
    fun getSessionMetrics(userId: UUID): SmartSessionMetrics? {
        // Fetch all SmartSessions belonging to the user
        val sessions = smartSessionRepository.findAllByUserId(userId)
        // Return null if no data is available (nothing to compute)
        if (sessions.isEmpty()) {
            return null
        }

        // Compute and return the aggregated metrics from session data
        return buildSmartSessionMetrics(sessions)
    }

    /**
     * Computes metrics for a filtered list of SmartSession IDs.
     *
     * @param userId the UUID of the user (used to verify ownership)
     * @param ids list of SmartSession IDs to evaluate
     * @return metrics or null if the list is empty or filtered out
     */
    fun getSessionMetrics(userId: UUID, ids: List<UUID>): SmartSessionMetrics? {
        // Load sessions by ID and filter to only include those owned by the current user
        val sessions = smartSessionRepository.findAllById(ids).filter { it.userId == userId }

        // If no valid sessions remain, return null
        if (sessions.isEmpty()) {
            return null
        }

        // Build and return aggregated metrics
        return buildSmartSessionMetrics(sessions)
    }

    /**
     * Aggregates all relevant metrics from a list of SmartSessions.
     *
     * @param sessions the list of SmartSession entities
     * @return a fully populated SmartSessionMetrics object
     */
    private fun buildSmartSessionMetrics(sessions: List<SmartSession>): SmartSessionMetrics {
        // Extract all non-null transcriptions and summaries from the sessions
        val transcriptions = sessions.mapNotNull { it.transcription }
        val summaries = sessions.mapNotNull { it.summary }

        return SmartSessionMetrics(
            dailyHeatmap = buildHeatmap(sessions),                               // Heatmap of activity per day
            transcriptionMetrics = buildTranscriptionMetrics(transcriptions),    // Metrics from transcription data
            summaryMetrics = buildSummaryMetrics(summaries)                      // Metrics from summary data
        )
    }

    /**
     * Builds transcription-specific metrics from a list of transcriptions.
     *
     * @param transcriptions List of Transcription entities
     * @return TranscriptionMetrics object with aggregated results
     */
    private fun buildTranscriptionMetrics(transcriptions: List<Transcription>): TranscriptionMetrics {
        return TranscriptionMetrics(
            totalTranscriptions = transcriptions.size,                                      // Number of transcriptions
            textStats = buildTextStats(transcriptions.mapNotNull { it.text }),              // Length-related stats
            createTimeStats = buildCreateTimeStats(transcriptions.mapNotNull { it.time }),  // Duration stats
            activityStats = buildActivityStats(transcriptions.map { it.createdAt })         // Temporal activity
        )
    }

    /**
     * Builds summary-specific metrics from a list of summaries.
     *
     * @param summaries List of Summary entities
     * @return SummaryMetrics object with aggregated results
     */
    private fun buildSummaryMetrics(summaries: List<Summary>): SummaryMetrics {
        return SummaryMetrics(
            totalSummaries = summaries.size,                                            // Number of summaries
            textStats = buildTextStats(summaries.mapNotNull { it.text }),               // Stats on summary lengths
            createTimeStats = buildCreateTimeStats(summaries.mapNotNull { it.time }),   // Duration stats
            activityStats = buildActivityStats(summaries.map { it.createdAt })          // Temporal activity
        )
    }

    /**
     * Computes basic statistics (average, min, max) on text lengths.
     *
     * @param texts A list of strings (e.g., transcriptions or summaries)
     * @return TextStats object with average, maximum, and minimum lengths
     */
    private fun buildTextStats(texts: List<String>): TextStats {
        // Map each string to its length
        val lengths = texts.map { it.length }

        return TextStats(
            averageLength = lengths.average(),  // Calculate average length
            maxLength = lengths.maxOrNull(),    // Maximum length (or null if empty)
            minLength = lengths.minOrNull()     // Minimum length (or null if empty)
        )
    }

    /**
     * Computes creation time statistics including average, median, max, and min durations.
     *
     * @param times A list of durations (e.g., processing times in ms)
     * @return CreateTimeStats with descriptive timing metrics
     */
    private fun buildCreateTimeStats(times: List<Long>): CreateTimeStats {
        val sorted = times.sorted()  // Sort times for median calculation
        return CreateTimeStats(
            average = sorted.average(),                         // Average time
            median = sorted.getOrNull(sorted.size / 2),   // Median (or null if empty)
            max = sorted.maxOrNull(),                           // Maximum creation time
            min = sorted.minOrNull()                             // Minimum creation time
        )
    }

    /**
     * Analyzes temporal activity based on timestamps.
     *
     * @param timestamps A list of epoch milliseconds representing creation times
     * @return ActivityStats including first/last activity, most active weekday, and average per day
     */
    private fun buildActivityStats(timestamps: List<Long>): ActivityStats {
        val zone = ZoneId.systemDefault()

        // Group timestamps by local date
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

    /**
     * Builds a heatmap representing how many SmartSessions were created per day.
     *
     * @param sessions A list of SmartSession entities
     * @return List of HeatmapCell objects, one for each date
     */
    private fun buildHeatmap(sessions: List<SmartSession>): List<HeatmapCell> {
        val zone = ZoneId.systemDefault()

        // Group sessions by local date and count how many per day
        return sessions.groupingBy { Instant.ofEpochMilli(it.createdAt).atZone(zone).toLocalDate() }.eachCount()
            .map { (date, count) -> HeatmapCell(date.toString(), count) }
    }
}