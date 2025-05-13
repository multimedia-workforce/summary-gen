package jku.multimediasysteme.analytics.data.metrics

import jku.multimediasysteme.analytics.data.metrics.heatmap.HeatmapCell

/**
 * Aggregated metrics for a single SmartSession or group of SmartSessions.
 *
 * @property dailyHeatmap List of daily activity for heatmap visualization.
 * @property transcriptionMetrics Metrics calculated from transcription content.
 * @property summaryMetrics Metrics calculated from summary content.
 */
data class SmartSessionMetrics(
    val dailyHeatmap: List<HeatmapCell>?,
    val transcriptionMetrics: TranscriptionMetrics?,
    val summaryMetrics: SummaryMetrics?
)