package com.planify.planifyspring.main.features.actions.domain.repositories

import com.planify.planifyspring.main.features.actions.domain.entities.Action

interface ActionsRepository {
    fun createAction(scope: String, type: String, data: Any): Action

    fun getIncomingActions(
        scope: String,
        group: String,
        consumer: String,
        count: Long,
        timeout: Long
    ): List<Action>
}
