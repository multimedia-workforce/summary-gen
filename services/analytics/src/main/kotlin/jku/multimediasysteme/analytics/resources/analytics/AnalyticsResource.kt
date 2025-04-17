package jku.multimediasysteme.analytics.resources.analytics

import jku.multimediasysteme.analytics.data.metrics.SmartSessionMetrics
import jku.multimediasysteme.analytics.service.metrics.MetricsService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/api/analytics")
class AnalyticsResource(private val metricsService: MetricsService) {
    @GetMapping("/global")
    fun getGlobalDashboardMetrics(@AuthenticationPrincipal userId: String): ResponseEntity<SmartSessionMetrics> {
        val metrics = metricsService.getSessionMetrics(UUID.fromString(userId))
        return metrics?.let { return ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()
    }

    @GetMapping("/selected")
    fun getSelectedDashboardMetrics(
        @AuthenticationPrincipal userId: String,
        @RequestBody ids: List<UUID>
    ): ResponseEntity<SmartSessionMetrics> {
        val metrics = metricsService.getSessionMetrics(UUID.fromString(userId), ids)
        return metrics?.let { return ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()
    }
}