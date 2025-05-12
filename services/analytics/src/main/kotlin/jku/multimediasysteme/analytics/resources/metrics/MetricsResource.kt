package jku.multimediasysteme.analytics.resources.metrics

import jku.multimediasysteme.analytics.data.IdsRequest
import jku.multimediasysteme.analytics.data.metrics.SmartSessionMetrics
import jku.multimediasysteme.analytics.service.metrics.MetricsService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 * REST controller that exposes endpoints for retrieving statistical metrics
 * related to SmartSessions, including activity, timing, and text statistics.
 *
 * @property metricsService Service for computing session-based metrics.
 */
@RestController
@RequestMapping("/metrics")
class MetricsResource(private val metricsService: MetricsService) {

    /**
     * Returns aggregated metrics for all SmartSessions of the authenticated user.
     *
     * Includes statistics like:
     * - Number of transcriptions/summaries
     * - Average length and creation time
     * - Activity heatmap
     *
     * @param userId Extracted user ID from JWT.
     * @return [SmartSessionMetrics] object or 404 if no sessions found.
     */
    @GetMapping
    fun getMetrics(@AuthenticationPrincipal userId: String): ResponseEntity<SmartSessionMetrics> {
        val metrics = metricsService.getSessionMetrics(UUID.fromString(userId))
        return metrics?.let { return ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()
    }

    /**
     * Returns metrics for a selected list of SmartSession IDs.
     *
     * Enables focused analysis based on session selection.
     *
     * @param userId Authenticated user ID extracted from JWT.
     * @param body Request body containing selected session UUIDs.
     * @return Filtered [SmartSessionMetrics] object or 404 if none found.
     */
    @PostMapping
    fun getSelectedMetrics(
        @AuthenticationPrincipal userId: String,
        @RequestBody body: IdsRequest
    ): ResponseEntity<SmartSessionMetrics> {
        val metrics = metricsService.getSessionMetrics(UUID.fromString(userId), body.ids)
        return metrics?.let { return ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()
    }
}