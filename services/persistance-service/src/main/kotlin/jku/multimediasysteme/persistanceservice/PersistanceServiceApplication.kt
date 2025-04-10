package jku.multimediasysteme.persistanceservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PersistanceServiceApplication

fun main(args: Array<String>) {
    runApplication<PersistanceServiceApplication>(*args)
}
