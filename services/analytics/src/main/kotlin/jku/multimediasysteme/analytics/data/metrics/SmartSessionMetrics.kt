package jku.multimediasysteme.analytics.data.metrics

import jku.multimediasysteme.analytics.data.metrics.heatmap.HeatmapCell

data class SmartSessionMetrics(
    val dailyHeatmap: List<HeatmapCell>?,
    private val transcriptionMetrics: TranscriptionMetrics?,
    private val summaryMetrics: SummaryMetrics?
)