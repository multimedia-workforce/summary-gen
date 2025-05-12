package jku.multimediasysteme.persistence

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

/**
 * Entry point for the Persistence Service application.
 * This service handles user authentication and storage of transcripts/summaries.
 */
@SpringBootApplication
// Enables scanning for Spring Data JPA repositories in the given base package
@EnableJpaRepositories("jku.multimediasysteme")
// Registers all JPA entities found in the specified package
@EntityScan("jku.multimediasysteme")
// Enables component scanning for all beans in the specified package
@ComponentScan("jku.multimediasysteme")
class PersistenceServiceApplication

/**
 * Bootstraps the Spring Boot application.
 */
fun main(args: Array<String>) {
    runApplication<PersistenceServiceApplication>(*args)
}
