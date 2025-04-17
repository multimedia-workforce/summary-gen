package jku.multimediasysteme.analytics.data.metrics

import jku.multimediasysteme.analytics.data.metrics.heatmap.HeatmapCell

data class SmartSessionMetrics(
    val dailyHeatmap: List<HeatmapCell>?,
    val transcriptionMetrics: TranscriptionMetrics?,
    val summaryMetrics: SummaryMetrics?
)