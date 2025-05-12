package jku.multimediasysteme.analytics.data.metrics.stats

/**
 * Describes user activity patterns over time.
 *
 * @property firstCreatedAt Timestamp of the earliest session.
 * @property lastCreatedAt Timestamp of the most recent session.
 * @property mostActiveWeekday Day with the highest number of created sessions.
 * @property avgPerDay Average number of sessions per day.
 */
data class ActivityStats(
    val firstCreatedAt: Long?,
    val lastCreatedAt: Long?,
    val mostActiveWeekday: String?,
    val avgPerDay: Double
)