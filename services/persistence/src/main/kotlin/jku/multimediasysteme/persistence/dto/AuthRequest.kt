package jku.multimediasysteme.persistence.dto

/**
 * Represents a login or registration request containing user credentials.
 *
 * @property username the user's chosen or registered username
 * @property password the user's password (plain text; will be hashed during registration)
 */
data class AuthRequest(val username: String, val password: String)
