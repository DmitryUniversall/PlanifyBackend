package com.planify.planifyspring.main.features.auth.data.dto

import com.planify.planifyspring.main.features.auth.domain.entities.RegisterConfirmationInfo
import java.io.Serializable
import java.time.Instant

data class RegisterConfirmationInfoDTO(
    val uuid: String,
    val code: Int,
    val userId: Long,
    val email: String,
    val createdAt: String,
    val updatedAt: String
) : Serializable {
    companion object {
        fun fromEntity(entity: RegisterConfirmationInfo): RegisterConfirmationInfoDTO = RegisterConfirmationInfoDTO(
            uuid = entity.uuid,
            code = entity.code,
            userId = entity.userId,
            email = entity.email,
            createdAt = entity.createdAt.toString(),
            updatedAt = entity.updatedAt.toString()
        )
    }

    fun toEntity(): RegisterConfirmationInfo = RegisterConfirmationInfo(
        uuid = uuid,
        code = code,
        userId = userId,
        email = email,
        createdAt = Instant.parse(createdAt),
        updatedAt = Instant.parse(updatedAt)
    )
}
