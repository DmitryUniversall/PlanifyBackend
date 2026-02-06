package com.planify.planifyspring.main.features.actions.domain.repositories

import com.planify.planifyspring.main.features.actions.domain.entities.Action
import com.planify.planifyspring.main.features.actions.domain.schemas.PatchActionScheme

interface ActionsRepository {
    fun createAction(scope: String, type: String, data: Any): Action

    fun getIncomingActions(scope: String, lastSeen: String, count: Long, timeout: Long): List<Action>

    fun patchAction(scope: String, actionId: String, patch: PatchActionScheme)
}
