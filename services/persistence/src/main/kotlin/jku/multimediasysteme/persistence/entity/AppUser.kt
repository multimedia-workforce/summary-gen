package jku.multimediasysteme.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.util.*

/**
 * Entity representing a registered user in the system.
 *
 * @property id Unique identifier of the user (UUID).
 * @property username Chosen username of the user.
 * @property password The user's hashed password (BCrypt).
 */
@Entity
data class AppUser(
    @Id
    val id: UUID = UUID.randomUUID(),
    val username: String,
    val password: String
)