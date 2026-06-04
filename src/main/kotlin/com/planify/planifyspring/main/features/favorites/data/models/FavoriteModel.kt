package com.planify.planifyspring.main.features.favorites.data.models

import com.planify.planifyspring.main.features.favorites.domain.entities.FavoriteRecord
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "favorites")
open class FavoriteModel(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null,

    @Column(nullable = false)
    open var userId: Long,

    @Column(nullable = false)
    open var favoriteUserId: Long,

    @Column(nullable = false)
    open var createdAt: Instant,
) {
    fun toEntity(): FavoriteRecord {
        return FavoriteRecord(
            userId = userId,
            favoriteUserId = favoriteUserId,
            createdAt = createdAt
        )
    }
}
