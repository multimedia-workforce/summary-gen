package jku.multimediasysteme.persistence

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
class PersistenceServiceApplication

fun main(args: Array<String>) {
    runApplication<PersistenceServiceApplication>(*args)
}
