package jku.multimediasysteme.analytics.data.metrics

import jku.multimediasysteme.analytics.data.metrics.stats.ActivityStats
import jku.multimediasysteme.analytics.data.metrics.stats.CreateTimeStats
import jku.multimediasysteme.analytics.data.metrics.stats.TextStats

/**
 * Metrics derived from a user's transcriptions.
 *
 * @property totalTranscriptions Number of transcriptions.
 * @property textStats Statistics about the text lengths.
 * @property createTimeStats Statistics about when transcriptions were created.
 * @property activityStats User activity patterns over time.
 */
data class TranscriptionMetrics(
    val totalTranscriptions: Int,
    val textStats: TextStats,
    val createTimeStats: CreateTimeStats,
    val activityStats: ActivityStats,
)