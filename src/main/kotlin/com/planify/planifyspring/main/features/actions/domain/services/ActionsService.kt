package com.planify.planifyspring.main.features.actions.domain.services

import com.planify.planifyspring.main.features.actions.domain.entities.Action

interface ActionsService {
    fun <T : Any> createAction(type: String, targetUserId: Long, data: T): Action<T>

    fun getActionByUuid(uuid: String): Action<out Any>?

    fun getUserIncomingActions(userId: Long, count: Long, timeout: Long): List<Action<out Any>>
}
