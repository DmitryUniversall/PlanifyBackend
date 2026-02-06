package com.planify.planifyspring.main.features.actions.domain.services_impl

import com.planify.planifyspring.main.features.actions.domain.entities.Action
import com.planify.planifyspring.main.features.actions.domain.repositories.ActionsRepository
import com.planify.planifyspring.main.features.actions.domain.schemas.PatchActionScheme
import com.planify.planifyspring.main.features.actions.domain.services.ActionsService
import org.springframework.stereotype.Service

@Service
class ActionsServiceImpl(
    val actionsRepository: ActionsRepository
) : ActionsService {
    override fun getUserActionsScope(userId: Long): String {
        return "users:$userId"
    }

    override fun createAction(scope: String, type: String, data: Any): Action {
        return actionsRepository.createAction(scope, type, data)
    }

    override fun createUserAction(userId: Long, type: String, data: Any): Action {
        val scope = getUserActionsScope(userId)
        return actionsRepository.createAction(scope, type, data)
    }

    override fun getIncomingActions(scope: String, lastSeen: String, count: Long, timeout: Long): List<Action> {
        return actionsRepository.getIncomingActions(scope, lastSeen, count, timeout)
    }

    override fun getUserIncomingActions(
        userId: Long,
        lastSeen: String,
        count: Long,
        timeout: Long
    ): List<Action> {
        val scope = getUserActionsScope(userId)
        return actionsRepository.getIncomingActions(scope, lastSeen, count, timeout)
    }

    override fun patchAction(scope: String, actionId: String, patch: PatchActionScheme) {
        return actionsRepository.patchAction(scope, actionId, patch)
    }

    override fun patchUserAction(userId: Long, actionId: String, patch: PatchActionScheme) {
        val scope = getUserActionsScope(userId)
        return actionsRepository.patchAction(scope, actionId, patch)
    }
}
