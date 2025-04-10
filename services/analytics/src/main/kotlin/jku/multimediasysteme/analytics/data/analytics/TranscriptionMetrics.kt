package jku.multimediasysteme.analytics.data.analytics

data class TranscriptionMetrics(
    val totalTranscriptions: Int,
    val summaryStats: SummaryLengthStats,
    //val transcriptionTimeStats: TranscriptionTimeStats,
    val activityStats: ActivityStats,
    val dailyHeatmap: List<HeatmapCell>
)