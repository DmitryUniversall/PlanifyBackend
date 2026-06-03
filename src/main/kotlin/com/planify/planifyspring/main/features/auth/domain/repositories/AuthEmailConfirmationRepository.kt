package com.planify.planifyspring.main.features.auth.domain.repositories

import com.planify.planifyspring.main.features.auth.domain.entities.RegisterConfirmationInfo

interface AuthEmailConfirmationRepository {
    fun saveRegisterConfirmationInfo(info: RegisterConfirmationInfo)
    fun getRegisterConfirmationInfo(uuid: String): RegisterConfirmationInfo?
}
