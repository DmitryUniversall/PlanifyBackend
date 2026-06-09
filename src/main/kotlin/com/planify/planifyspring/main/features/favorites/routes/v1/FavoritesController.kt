package com.planify.planifyspring.main.features.favorites.routes.v1

import com.planify.planifyspring.main.common.entities.ApplicationResponse
import com.planify.planifyspring.main.common.utils.asSuccessApplicationResponse
import com.planify.planifyspring.main.features.auth.domain.entities.AuthContext
import com.planify.planifyspring.main.features.favorites.domain.services.FavoritesService
import com.planify.planifyspring.main.features.favorites.routes.v1.dto.FavoriteRecordDTO
import com.planify.planifyspring.main.features.favorites.routes.v1.dto.add_favorite.AddFavoriteRequestDTO
import com.planify.planifyspring.main.features.favorites.routes.v1.dto.get_favorites.GetUserFavoritesResponseDTO
import com.planify.planifyspring.main.features.favorites.routes.v1.dto.remove_favorite.RemoveFavoriteRequestDTO
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/favorites/")
class FavoritesController(
    private val favoritesService: FavoritesService
) {
    @GetMapping("/my")
    fun getUserFavorites(
        @AuthenticationPrincipal principal: AuthContext
    ): ResponseEntity<ApplicationResponse<GetUserFavoritesResponseDTO>> {
        val userId = principal.user.id

        return ResponseEntity.ok(
            GetUserFavoritesResponseDTO(
                favorites = favoritesService.getUserFavorites(userId).map { FavoriteRecordDTO.fromEntity(it) }
            ).asSuccessApplicationResponse()
        )
    }

    @PostMapping("/my")
    fun addFavorite(
        @RequestBody body: AddFavoriteRequestDTO,
        @AuthenticationPrincipal principal: AuthContext
    ): ResponseEntity<ApplicationResponse<Nothing>> {
        val userId = principal.user.id
        favoritesService.addFavorite(userId, body.favoriteUserId)
        return ResponseEntity.ok(ApplicationResponse.success())
    }

    @DeleteMapping("/my")
    fun removeFavorite(
        @RequestBody body: RemoveFavoriteRequestDTO,  // TODO: Use path param?
        @AuthenticationPrincipal principal: AuthContext
    ): ResponseEntity<ApplicationResponse<Nothing>> {
        val userId = principal.user.id
        favoritesService.removeFavorite(userId, body.favoriteUserId)
        return ResponseEntity.ok(ApplicationResponse.success())
    }
}
