package jku.multimediasysteme.analytics.resources.analytics

import jku.multimediasysteme.analytics.data.metrics.SmartSessionMetrics
import jku.multimediasysteme.analytics.service.metrics.MetricsService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/analytics")
class AnalyticsResource(private val metricsService: MetricsService) {
    @GetMapping("/analytics/global")
    fun getGlobalDashboardMetrics(@RequestParam userId: UUID): ResponseEntity<SmartSessionMetrics> {
        val metrics = metricsService.getSessionMetrics(userId)
        return ResponseEntity.ok(metrics)
    }

    @GetMapping("/analytics/selected")
    fun getSelectedDashboardMetrics(
        @RequestParam userId: UUID,
        @RequestBody ids: List<UUID>
    ): ResponseEntity<SmartSessionMetrics> {
        val metrics = metricsService.getSessionMetrics(userId, ids)
        return ResponseEntity.ok(metrics)
    }
}