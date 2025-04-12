package jku.multimediasysteme.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.util.*

@Entity
data class AppUser(
    @Id
    val id: UUID = UUID.randomUUID(),
    val username: String,
    val password: String
)