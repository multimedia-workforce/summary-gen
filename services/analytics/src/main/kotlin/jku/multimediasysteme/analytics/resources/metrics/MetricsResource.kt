package jku.multimediasysteme.analytics.resources.metrics

import jku.multimediasysteme.analytics.data.IdsRequest
import jku.multimediasysteme.analytics.data.metrics.SmartSessionMetrics
import jku.multimediasysteme.analytics.service.metrics.MetricsService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/metrics")
class MetricsResource(private val metricsService: MetricsService) {
    @GetMapping
    fun getMetrics(@AuthenticationPrincipal userId: String): ResponseEntity<SmartSessionMetrics> {
        val metrics = metricsService.getSessionMetrics(UUID.fromString(userId))
        return metrics?.let { return ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()
    }

    @PostMapping
    fun getSelectedMetrics(
        @AuthenticationPrincipal userId: String,
        @RequestBody body: IdsRequest
    ): ResponseEntity<SmartSessionMetrics> {
        val metrics = metricsService.getSessionMetrics(UUID.fromString(userId), body.ids)
        return metrics?.let { return ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()
    }
}