package com.planify.planifyspring.main.features.actions.domain.services

import com.planify.planifyspring.main.features.actions.domain.entities.Action

interface ActionsService {
    fun createAction(scope: String, type: String, data: Any): Action

    fun createUserAction(userId: Long, type: String, data: Any): Action

    fun getUserIncomingActions(userId: Long, sessionUuid: String, count: Long, timeout: Long): List<Action>
}
