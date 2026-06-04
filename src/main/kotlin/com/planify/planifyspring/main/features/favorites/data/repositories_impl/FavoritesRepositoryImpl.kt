package com.planify.planifyspring.main.features.favorites.data.repositories_impl

import com.planify.planifyspring.main.features.favorites.data.jpa.FavoritesJPARepository
import com.planify.planifyspring.main.features.favorites.data.models.FavoriteModel
import com.planify.planifyspring.main.features.favorites.domain.entities.FavoriteRecord
import com.planify.planifyspring.main.features.favorites.domain.repositories.FavoritesRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class FavoritesRepositoryImpl(
    private val jpaRepository: FavoritesJPARepository
) : FavoritesRepository {
    override fun getUserFavorites(userId: Long): List<FavoriteRecord> {
        return jpaRepository.findAllByUserId(userId).map { it.toEntity() }
    }

    override fun createFavorite(userId: Long, favoriteUserId: Long) {
        val model = FavoriteModel(
            userId = userId,
            favoriteUserId = favoriteUserId,
            createdAt = LocalDateTime.now()
        )

        jpaRepository.save(model)
    }

    override fun deleteFavorite(userId: Long, favoriteUserId: Long) {
        jpaRepository.deleteByUserIdAndFavoriteUserId(userId, favoriteUserId)
    }

    override fun existsByUserIdAndFavoriteUserId(userId: Long, favoriteUserId: Long): Boolean {
        return jpaRepository.existsByUserIdAndFavoriteUserId(userId, favoriteUserId)
    }
}
