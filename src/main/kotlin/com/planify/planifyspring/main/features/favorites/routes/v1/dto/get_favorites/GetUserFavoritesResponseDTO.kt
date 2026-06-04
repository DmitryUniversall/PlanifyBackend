package com.planify.planifyspring.main.features.favorites.routes.v1.dto.get_favorites

import com.planify.planifyspring.main.features.favorites.routes.v1.dto.FavoriteRecordDTO

class GetUserFavoritesResponseDTO(
    val favorites: List<FavoriteRecordDTO>
)
