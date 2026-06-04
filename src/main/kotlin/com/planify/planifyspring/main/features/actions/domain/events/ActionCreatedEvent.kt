package com.planify.planifyspring.main.features.actions.domain.events

import com.planify.planifyspring.main.features.actions.domain.entities.Action

class ActionCreatedEvent(
    val userId: Long,
    val action: Action
)
