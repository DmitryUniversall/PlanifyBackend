package com.planify.planifyspring.main.features.actions.routing

import com.planify.planifyspring.main.common.entities.ApplicationResponse
import com.planify.planifyspring.main.common.utils.asSuccessApplicationResponse
import com.planify.planifyspring.main.features.actions.domain.services.ActionsService
import com.planify.planifyspring.main.features.actions.routing.dto.GetIncomingActionsResponseDTO
import com.planify.planifyspring.main.features.auth.domain.entities.AuthContext
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/actions")
class ActionsController(
    val actionsService: ActionsService
) {
    @GetMapping("/my/incoming")
    fun getIncomingActions(
        @AuthenticationPrincipal authContext: AuthContext,
        @RequestParam count: Long = 10,
        @RequestParam timeout: Long = 30,
    ): ResponseEntity<ApplicationResponse<GetIncomingActionsResponseDTO>> {
        val actions = actionsService.getUserIncomingActions(
            userId = authContext.user.id,
            sessionUuid = authContext.session.uuid,
            count = count,
            timeout = timeout
        )

        return ResponseEntity.ok(
            GetIncomingActionsResponseDTO(
                actions = actions.map { ActionDTO.fromEntity(it) }
            ).asSuccessApplicationResponse()
        )
    }
}
