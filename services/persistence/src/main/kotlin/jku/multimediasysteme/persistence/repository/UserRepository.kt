package jku.multimediasysteme.persistence.repository

import jku.multimediasysteme.persistence.entity.AppUser
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserRepository : JpaRepository<AppUser, UUID> {
    fun findByUsername(username: String): AppUser?
    fun existsByUsername(username: String): Boolean
}