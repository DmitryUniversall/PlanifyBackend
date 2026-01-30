package com.planify.planifyspring.main.features.actions.domain.services

import com.planify.planifyspring.main.features.actions.domain.entities.Action

interface ActionsService {
    fun <T : Any> createAction(
        type: String,
        senderId: Long,
        targetId: Long,
        data: T
    ): Action<T>

    fun <T : Any> getActionByUuid(uuid: String): Action<T>?

    fun getUserIncomingActions(type: String? = null): List<Action<Any>>

    fun getUserOutgoingActions(type: String? = null): List<Action<Any>>
}
