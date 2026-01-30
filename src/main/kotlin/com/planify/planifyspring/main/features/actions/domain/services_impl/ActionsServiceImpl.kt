package com.planify.planifyspring.main.features.actions.domain.services_impl

import com.planify.planifyspring.main.features.actions.domain.entities.Action
import com.planify.planifyspring.main.features.actions.domain.repositories.ActionsRepository
import com.planify.planifyspring.main.features.actions.domain.services.ActionsService
import org.springframework.stereotype.Service

@Service
class ActionsServiceImpl(
    val actionsRepository: ActionsRepository
) : ActionsService {
    override fun <T : Any> createAction(type: String, targetUserId: Long, data: T): Action<T> {
        return actionsRepository.createAction(type, targetUserId, data)
    }

    override fun getActionByUuid(uuid: String): Action<out Any>? {
        return actionsRepository.getActionByUuid(uuid)
    }

    override fun getUserIncomingActions(userId: Long, count: Long, timeout: Long): List<Action<out Any>> {
        return actionsRepository.getUserIncomingActions(userId, count, timeout)
    }
}