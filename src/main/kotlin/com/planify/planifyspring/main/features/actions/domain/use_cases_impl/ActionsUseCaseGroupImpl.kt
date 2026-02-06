package com.planify.planifyspring.main.features.actions.domain.use_cases_impl

import com.planify.planifyspring.core.exceptions.AlreadyInUseAppError
import com.planify.planifyspring.main.exceptions.generics.AlreadyInUseHttpException
import com.planify.planifyspring.main.features.actions.domain.entities.Action
import com.planify.planifyspring.main.features.actions.domain.schemas.PatchActionScheme
import com.planify.planifyspring.main.features.actions.domain.services.ActionsService
import com.planify.planifyspring.main.features.actions.domain.use_cases.ActionsUseCaseGroup
import org.springframework.stereotype.Component

@Component
class ActionsUseCaseGroupImpl(
    val actionsService: ActionsService
) : ActionsUseCaseGroup {
    override fun createAction(scope: String, type: String, data: Any): Action {
        return actionsService.createAction(scope, type, data)
    }

    override fun createUserAction(userId: Long, type: String, data: Any): Action {
        return actionsService.createUserAction(userId, type, data)
    }

    override fun getIncomingActions(
        scope: String,
        lastSeen: String,
        count: Long,
        timeout: Long
    ): List<Action> {
        return actionsService.getIncomingActions(scope, lastSeen, count, timeout)
    }

    override fun getUserIncomingActions(userId: Long, lastSeen: String, count: Long, timeout: Long): List<Action> {
        try {
            return actionsService.getUserIncomingActions(userId, lastSeen, count, timeout)
        } catch (_: AlreadyInUseAppError) {
            throw AlreadyInUseHttpException("This customer is already reading actions")
        }
    }

    override fun patchAction(scope: String, actionId: String, patch: PatchActionScheme) {
        return actionsService.patchAction(scope, actionId, patch)
    }

    override fun patchUserAction(userId: Long, actionId: String, patch: PatchActionScheme) {
        return actionsService.patchUserAction(userId, actionId, patch)
    }
}
