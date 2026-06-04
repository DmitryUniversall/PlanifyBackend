package com.planify.planifyspring.main.features.favorites.data.jpa

import com.planify.planifyspring.main.features.favorites.data.models.FavoriteModel
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FavoritesJPARepository : JpaRepository<FavoriteModel, Long> {
    fun findAllByUserId(userId: Long): List<FavoriteModel>
    fun deleteByUserIdAndFavoriteUserId(userId: Long, favoriteUserId: Long)

    fun existsByUserIdAndFavoriteUserId(userId: Long, favoriteUserId: Long): Boolean
}
