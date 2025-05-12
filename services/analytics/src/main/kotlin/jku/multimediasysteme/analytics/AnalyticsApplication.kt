package jku.multimediasysteme.analytics

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

/**
 * Entry point for the Analytics Service application.
 * This service handles metrics of SmartSessionSummaries.
 */
@SpringBootApplication
// Enables scanning for Spring Data JPA repositories in the given base package
@EnableJpaRepositories("jku.multimediasysteme")
// Registers all JPA entities found in the specified package
@EntityScan("jku.multimediasysteme")
// Enables component scanning for all beans in the specified package
@ComponentScan("jku.multimediasysteme")
class AnalyticsApplication

/**
 * Bootstraps the Spring Boot application.
 */
fun main(args: Array<String>) {
    runApplication<AnalyticsApplication>(*args)
}
