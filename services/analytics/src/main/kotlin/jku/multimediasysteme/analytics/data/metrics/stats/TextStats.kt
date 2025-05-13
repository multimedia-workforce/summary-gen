package jku.multimediasysteme.analytics.data.metrics.stats

/**
 * Statistics about the length of textual entries (e.g. summaries, transcriptions).
 *
 * @property averageLength Average text length.
 * @property maxLength Longest text length.
 * @property minLength Shortest text length.
 */
data class TextStats(
    val averageLength: Double,
    val maxLength: Int?,
    val minLength: Int?
)