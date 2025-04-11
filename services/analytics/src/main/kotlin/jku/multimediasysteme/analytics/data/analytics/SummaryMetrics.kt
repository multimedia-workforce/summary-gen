package jku.multimediasysteme.analytics.data.analytics

import jku.multimediasysteme.analytics.data.analytics.stats.ActivityStats
import jku.multimediasysteme.analytics.data.analytics.stats.CreateTimeStats
import jku.multimediasysteme.analytics.data.analytics.stats.TextStats

data class SummaryMetrics(
    val totalTranscriptions: Int,
    val textStats: TextStats,
    val createTimeStats: CreateTimeStats,
    val activityStats: ActivityStats,
)