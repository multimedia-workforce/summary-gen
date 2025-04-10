package jku.multimediasysteme.analytics.data.analytics

data class ActivityStats(
    val firstCreatedAt: Long?,
    val lastCreatedAt: Long?,
    val mostActiveWeekday: String?,
    val avgPerDay: Double
)