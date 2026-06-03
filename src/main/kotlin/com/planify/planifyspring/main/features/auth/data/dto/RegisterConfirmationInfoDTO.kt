package com.planify.planifyspring.main.features.auth.data.dto

import com.planify.planifyspring.main.features.auth.domain.entities.RegisterConfirmationInfo
import java.io.Serializable

data class RegisterConfirmationInfoDTO(
    val uuid: String,
    val code: Int,
    val userId: Long,
    val email: String,
) : Serializable {
    companion object {
        fun fromEntity(info: RegisterConfirmationInfo): RegisterConfirmationInfoDTO = RegisterConfirmationInfoDTO(
            uuid = info.uuid,
            code = info.code,
            userId = info.userId,
            email = info.email
        )
    }

    fun toEntity(): RegisterConfirmationInfo = RegisterConfirmationInfo(
        uuid = uuid,
        code = code,
        userId = userId,
        email = email
    )
}
