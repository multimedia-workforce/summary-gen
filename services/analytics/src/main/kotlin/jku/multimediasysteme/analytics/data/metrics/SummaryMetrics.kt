package jku.multimediasysteme.analytics.data.metrics

import jku.multimediasysteme.analytics.data.metrics.stats.ActivityStats
import jku.multimediasysteme.analytics.data.metrics.stats.CreateTimeStats
import jku.multimediasysteme.analytics.data.metrics.stats.TextStats

data class SummaryMetrics(
    val totalSummaries: Int,
    val textStats: TextStats,
    val createTimeStats: CreateTimeStats,
    val activityStats: ActivityStats,
)