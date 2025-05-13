package jku.multimediasysteme.analytics.data.metrics.heatmap

/**
 * Represents a single cell in a date-based activity heatmap.
 *
 * @property date The date in format YYYY-MM-DD.
 * @property count Number of activities (e.g. sessions) on that day.
 */
data class HeatmapCell(val date: String, val count: Int)