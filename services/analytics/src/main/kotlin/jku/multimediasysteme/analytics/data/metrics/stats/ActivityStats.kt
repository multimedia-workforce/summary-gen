package jku.multimediasysteme.analytics.data.metrics.stats

data class ActivityStats(
    val firstCreatedAt: Long?,
    val lastCreatedAt: Long?,
    val mostActiveWeekday: String?,
    val avgPerDay: Double
)