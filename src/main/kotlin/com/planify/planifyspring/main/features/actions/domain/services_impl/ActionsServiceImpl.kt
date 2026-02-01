package com.planify.planifyspring.main.features.actions.domain.services_impl

import com.planify.planifyspring.main.features.actions.domain.entities.Action
import com.planify.planifyspring.main.features.actions.domain.repositories.ActionsRepository
import com.planify.planifyspring.main.features.actions.domain.services.ActionsService
import org.springframework.stereotype.Service

@Service
class ActionsServiceImpl(
    val actionsRepository: ActionsRepository
) : ActionsService {
    private fun getUserActionsScope(userId: Long): String {
        return "users:$userId"
    }

    private fun getUserActionsGroup(userId: Long, sessionUuid: String): String {
        return "group-user-$userId-$sessionUuid"
    }

    private fun getUserActionsConsumer(userId: Long): String {
        return "consumer-user-$userId"
    }

    override fun createAction(scope: String, type: String, data: Any): Action {
        return actionsRepository.createAction(
            scope = scope,
            type = type,
            data = data
        )
    }

    override fun createUserAction(userId: Long, type: String, data: Any): Action {
        return actionsRepository.createAction(
            scope = getUserActionsScope(userId = userId),
            type = type,
            data = data
        )
    }

    override fun getUserIncomingActions(
        userId: Long,
        sessionUuid: String,
        count: Long,
        timeout: Long
    ): List<Action> {
        return actionsRepository.getIncomingActions(
            scope = getUserActionsScope(userId = userId),
            group = getUserActionsGroup(userId = userId, sessionUuid = sessionUuid),
            consumer = getUserActionsConsumer(userId = userId),
            count = count,
            timeout = timeout
        )
    }
}
