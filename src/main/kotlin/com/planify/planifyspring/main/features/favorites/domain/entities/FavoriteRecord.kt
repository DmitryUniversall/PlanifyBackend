package com.planify.planifyspring.main.features.favorites.domain.entities

import java.time.LocalDateTime

data class FavoriteRecord(
    val userId: Long,
    val favoriteUserId: Long,
    val createdAt: LocalDateTime,
)
