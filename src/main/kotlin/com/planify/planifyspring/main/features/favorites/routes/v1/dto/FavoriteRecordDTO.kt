package com.planify.planifyspring.main.features.favorites.routes.v1.dto

import com.planify.planifyspring.main.features.favorites.domain.entities.FavoriteRecord
import java.time.LocalDateTime

data class FavoriteRecordDTO(
    val userId: Long,
    val favoriteUserId: Long,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun fromEntity(entity: FavoriteRecord) = FavoriteRecordDTO(
            userId = entity.userId,
            favoriteUserId = entity.favoriteUserId,
            createdAt = entity.createdAt,
        )
    }
}
