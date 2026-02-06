package com.planify.planifyspring.main.features.actions.domain.services

import com.planify.planifyspring.main.features.actions.domain.entities.Action
import com.planify.planifyspring.main.features.actions.domain.schemas.PatchActionScheme

interface ActionsService {
    fun getUserActionsScope(userId: Long): String

    fun createAction(scope: String, type: String, data: Any): Action
    fun createUserAction(userId: Long, type: String, data: Any): Action

    fun getIncomingActions(scope: String, lastSeen: String, count: Long, timeout: Long): List<Action>
    fun getUserIncomingActions(userId: Long, lastSeen: String, count: Long, timeout: Long): List<Action>

    fun patchAction(scope: String, actionId: String, patch: PatchActionScheme)
    fun patchUserAction(userId: Long, actionId: String, patch: PatchActionScheme)
}
