package jku.multimediasysteme.analytics.data.analytics.stats

data class ActivityStats(
    val firstCreatedAt: Long?,
    val lastCreatedAt: Long?,
    val mostActiveWeekday: String?,
    val avgPerDay: Double
)