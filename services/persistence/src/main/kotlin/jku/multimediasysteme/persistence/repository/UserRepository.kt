package jku.multimediasysteme.persistence.repository

import jku.multimediasysteme.persistence.entity.AppUser
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

/**
 * Repository for managing user accounts.
 * Provides lookup methods based on username.
 */
interface UserRepository : JpaRepository<AppUser, UUID> {

    /**
     * Finds a user by their username.
     *
     * @param username the username to search for
     * @return the user entity if found, otherwise null
     */
    fun findByUsername(username: String): AppUser?
}