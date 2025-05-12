package jku.multimediasysteme.analytics.data

import java.util.*

/**
 * Request object for sending a list of SmartSession IDs to the backend.
 *
 * @property ids List of SmartSession UUIDs.
 */
data class IdsRequest(
    val ids: List<UUID>
)
