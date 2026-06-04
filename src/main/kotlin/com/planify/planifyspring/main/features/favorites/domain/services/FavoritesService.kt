package com.planify.planifyspring.main.features.favorites.domain.services

import com.planify.planifyspring.main.features.favorites.domain.entities.FavoriteRecord

interface FavoritesService {
    fun getUserFavorites(userId: Long): List<FavoriteRecord>
    fun addFavorite(userId: Long, favoriteUserId: Long)
    fun removeFavorite(userId: Long, favoriteUserId: Long)
}
