package jku.multimediasysteme.analytics.data

data class TranscriptionMetrics(
    val totalTranscriptions: Int,
    val averageSummaryLength: Double,
    val avgTranscriptionTime: Double? = null,
)
