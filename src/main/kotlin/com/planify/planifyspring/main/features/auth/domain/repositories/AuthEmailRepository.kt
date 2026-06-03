package com.planify.planifyspring.main.features.auth.domain.repositories

import com.planify.planifyspring.main.features.auth.domain.entities.PasswordRecoveryChallenge
import com.planify.planifyspring.main.features.auth.domain.entities.RegisterConfirmationInfo

interface AuthEmailRepository {
    fun saveRegisterConfirmationInfo(info: RegisterConfirmationInfo)
    fun getRegisterConfirmationInfo(uuid: String): RegisterConfirmationInfo?

    fun getRecoverPasswordChallenge(challengeUUID: String): PasswordRecoveryChallenge?
    fun saveRecoverPasswordChallenge(passwordRecoveryChallenge: PasswordRecoveryChallenge)

    fun getUserActiveRecoverPasswordChallengeUUID(userId: Long): String?

    fun deleteRecoverPasswordChallenge(challengeUUID: String, userId: Long)
}
