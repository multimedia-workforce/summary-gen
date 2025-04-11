package jku.multimediasysteme.analytics.data.analytics

import jku.multimediasysteme.analytics.data.analytics.heatmap.HeatmapCell

data class SmartSessionMetrics(
    val dailyHeatmap: List<HeatmapCell>?,
    private val transcriptionMetrics: TranscriptionMetrics?,
    private val summaryMetrics: SummaryMetrics?
)