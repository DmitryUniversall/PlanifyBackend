package com.planify.planifyspring.main.features.auth.data.repositories_impl

import com.planify.planifyspring.main.common.utils.redis.RedisHelper
import com.planify.planifyspring.main.features.auth.data.dto.RegisterConfirmationInfoDTO
import com.planify.planifyspring.main.features.auth.domain.entities.RegisterConfirmationInfo
import com.planify.planifyspring.main.features.auth.domain.repositories.AuthEmailConfirmationRepository
import org.springframework.stereotype.Repository

@Repository
class AuthEmailConfirmationRepositoryImpl(
    private val helper: RedisHelper
) : AuthEmailConfirmationRepository {
    private fun getConfirmationKey(uuid: String): String {
        return "auth:confirmation:$uuid"
    }

    override fun saveRegisterConfirmationInfo(info: RegisterConfirmationInfo) {
        val dto = RegisterConfirmationInfoDTO.fromEntity(info)
        helper.hset(getConfirmationKey(info.uuid), dto)
    }

    override fun getRegisterConfirmationInfo(uuid: String): RegisterConfirmationInfo? {
        val dto = helper.hget(getConfirmationKey(uuid), RegisterConfirmationInfoDTO::class.java)
        return dto?.toEntity()
    }
}
