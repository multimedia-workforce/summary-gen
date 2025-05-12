package jku.multimediasysteme.analytics.data.metrics.stats

/**
 * Statistics related to the creation timestamps of entries.
 *
 * @property average Average creation time (e.g. duration in ms).
 * @property median Median creation time.
 * @property max Longest recorded creation time.
 * @property min Shortest recorded creation time.
 */
data class CreateTimeStats(
    val average: Double,
    val median: Long?,
    val max: Long?,
    val min: Long?
)