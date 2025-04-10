package jku.multimediasysteme.shared.jpa

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import java.util.*

@MappedSuperclass
abstract class AbstractUserEntity(
    @Column(nullable = false)
    open var userId: UUID
)