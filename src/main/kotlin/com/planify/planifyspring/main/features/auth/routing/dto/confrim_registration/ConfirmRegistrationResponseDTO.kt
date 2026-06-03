package com.planify.planifyspring.main.features.auth.routing.dto.confrim_registration

import com.planify.planifyspring.main.features.auth.routing.dto.AccessInfoDTO
import com.planify.planifyspring.main.features.auth.routing.dto.AuthSessionPrivateDTO
import com.planify.planifyspring.main.features.auth.routing.dto.AuthTokenPairDTO
import com.planify.planifyspring.main.features.auth.routing.dto.UserPrivateDTO

data class ConfirmRegistrationResponseDTO(
    val user: UserPrivateDTO,
    val session: AuthSessionPrivateDTO,
    val tokens: AuthTokenPairDTO,
    val accessInfo: AccessInfoDTO
)
