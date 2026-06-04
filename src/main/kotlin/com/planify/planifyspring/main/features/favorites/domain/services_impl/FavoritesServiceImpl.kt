package com.planify.planifyspring.main.features.favorites.domain.services_impl

import com.planify.planifyspring.main.features.favorites.domain.entities.FavoriteRecord
import com.planify.planifyspring.main.features.favorites.domain.exceptions.UserAlreadyInFavoritesHttpException
import com.planify.planifyspring.main.features.favorites.domain.repositories.FavoritesRepository
import com.planify.planifyspring.main.features.favorites.domain.services.FavoritesService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FavoritesServiceImpl(
    private val favoritesRepository: FavoritesRepository
) : FavoritesService {
    @Transactional(readOnly = true)
    override fun getUserFavorites(userId: Long): List<FavoriteRecord> {
        return favoritesRepository.getUserFavorites(userId)
    }

    @Transactional
    override fun addFavorite(userId: Long, favoriteUserId: Long) {
        if (favoritesRepository.existsByUserIdAndFavoriteUserId(userId, favoriteUserId)) throw UserAlreadyInFavoritesHttpException("User already in favorites")

        return favoritesRepository.createFavorite(userId, favoriteUserId)
    }

    @Transactional
    override fun removeFavorite(userId: Long, favoriteUserId: Long) {
        favoritesRepository.deleteFavorite(userId, favoriteUserId)
    }
}
