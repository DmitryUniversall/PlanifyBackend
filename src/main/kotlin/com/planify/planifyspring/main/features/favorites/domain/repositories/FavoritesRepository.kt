package com.planify.planifyspring.main.features.favorites.domain.repositories

import com.planify.planifyspring.main.features.favorites.domain.entities.FavoriteRecord

interface FavoritesRepository {
    fun getUserFavorites(userId: Long): List<FavoriteRecord>
    fun createFavorite(userId: Long, favoriteUserId: Long)
    fun deleteFavorite(userId: Long, favoriteUserId: Long)

    fun existsByUserIdAndFavoriteUserId(userId: Long, favoriteUserId: Long): Boolean
}
