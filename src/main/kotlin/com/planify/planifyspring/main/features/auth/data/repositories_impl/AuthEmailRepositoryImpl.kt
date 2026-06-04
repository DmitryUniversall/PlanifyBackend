package com.planify.planifyspring.main.features.auth.data.repositories_impl

import com.planify.planifyspring.main.common.utils.redis.RedisHelper
import com.planify.planifyspring.main.features.auth.data.dto.PasswordRecoveryChallengeDTO
import com.planify.planifyspring.main.features.auth.data.dto.RegisterConfirmationInfoDTO
import com.planify.planifyspring.main.features.auth.domain.entities.PasswordRecoveryChallenge
import com.planify.planifyspring.main.features.auth.domain.entities.RegisterConfirmationInfo
import com.planify.planifyspring.main.features.auth.domain.repositories.AuthEmailRepository
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class AuthEmailRepositoryImpl(
    private val helper: RedisHelper
) : AuthEmailRepository {
    private fun getConfirmationKey(uuid: String): String {
        return "auth:confirmation:$uuid"
    }

    private fun getPasswordRecoveryKey(uuid: String): String {
        return "auth:password_recovery:$uuid"
    }

    private fun getUserActiveRecoveryChallengeKey(userId: Long): String {
        return "auth:password_recovery:user:$userId"
    }

    override fun saveRegisterConfirmationInfo(info: RegisterConfirmationInfo, ttl: Duration) {
        val key = getConfirmationKey(info.uuid)
        helper.hset(key, RegisterConfirmationInfoDTO.fromEntity(info))
        helper.expire(key, ttl)
    }

    override fun getRegisterConfirmationInfo(uuid: String): RegisterConfirmationInfo? {
        val dto = helper.hget(getConfirmationKey(uuid), RegisterConfirmationInfoDTO::class.java)
        return dto?.toEntity()
    }

    override fun saveRecoverPasswordChallenge(passwordRecoveryChallenge: PasswordRecoveryChallenge, ttl: Duration) {
        val key = getPasswordRecoveryKey(passwordRecoveryChallenge.uuid)
        helper.set(key, PasswordRecoveryChallengeDTO.fromEntity(passwordRecoveryChallenge))
        helper.expire(key, ttl)

        val userActiveChallengeKey = getUserActiveRecoveryChallengeKey(passwordRecoveryChallenge.userId)
        helper.set(userActiveChallengeKey, passwordRecoveryChallenge.uuid)
        helper.expire(userActiveChallengeKey, ttl)
    }

    override fun getRecoverPasswordChallenge(challengeUUID: String): PasswordRecoveryChallenge? {
        val dto = helper.get(getPasswordRecoveryKey(challengeUUID), PasswordRecoveryChallengeDTO::class.java)
        return dto?.toEntity()
    }

    override fun getUserActiveRecoverPasswordChallengeUUID(userId: Long): String? {
        return helper.get(getUserActiveRecoveryChallengeKey(userId), String::class.java)
    }

    override fun deleteRecoverPasswordChallenge(challengeUUID: String, userId: Long) {
        helper.del(getPasswordRecoveryKey(challengeUUID))
        helper.del(getUserActiveRecoveryChallengeKey(userId))
    }
}
