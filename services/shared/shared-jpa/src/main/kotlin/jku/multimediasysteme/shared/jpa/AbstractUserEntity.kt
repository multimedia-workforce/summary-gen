package jku.multimediasysteme.shared.jpa

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import java.util.*

/**
 * Base class for entities that are associated with a specific user.
 * Contains a shared userId field to track ownership.
 */
@MappedSuperclass
abstract class AbstractUserEntity(

    /**
     * The ID of the user who owns this entity.
     */
    @Column(nullable = false)
    open var userId: UUID
)