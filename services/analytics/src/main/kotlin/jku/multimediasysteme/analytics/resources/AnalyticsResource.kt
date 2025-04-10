package jku.multimediasysteme.analytics.resources

import jku.multimediasysteme.analytics.data.TranscriptionMetrics
import jku.multimediasysteme.analytics.service.AnalyticsService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/analytics")
class AnalyticsResource(private val analyticsService: AnalyticsService) {
    @GetMapping
    fun getGlobalDashboardMetrics(@RequestParam userId: UUID): ResponseEntity<TranscriptionMetrics> {
        val metrics = analyticsService.getTranscriptionMetrics(userId)
        return ResponseEntity.ok(metrics)
    }

    @GetMapping
    fun getSelectedDashboardMetrics(
        @RequestParam userId: UUID,
        @RequestBody ids: List<UUID>
    ): ResponseEntity<TranscriptionMetrics> {
        val metrics = analyticsService.getTranscriptionMetrics(userId, ids)
        return ResponseEntity.ok(metrics)
    }
}