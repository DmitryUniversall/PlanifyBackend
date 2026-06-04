package com.planify.planifyspring.main.features.favorites.routes.v1.dto

import com.planify.planifyspring.main.features.favorites.domain.entities.FavoriteRecord
import java.time.Instant

data class FavoriteRecordDTO(
    val userId: Long,
    val favoriteUserId: Long,
    val createdAt: Instant,
) {
    companion object {
        fun fromEntity(entity: FavoriteRecord) = FavoriteRecordDTO(
            userId = entity.userId,
            favoriteUserId = entity.favoriteUserId,
            createdAt = entity.createdAt,
        )
    }
}
