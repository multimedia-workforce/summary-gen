package jku.multimediasysteme.analytics

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EnableJpaRepositories("jku.multimediasysteme.shared.jpa.transcription.repository")
@EntityScan("jku.multimediasysteme.shared.jpa.transcription.model")
class AnalyticsApplication

fun main(args: Array<String>) {
    runApplication<AnalyticsApplication>(*args)
}
