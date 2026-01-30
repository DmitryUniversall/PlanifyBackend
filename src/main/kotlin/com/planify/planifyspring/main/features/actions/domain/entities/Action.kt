package com.planify.planifyspring.main.features.actions.domain.entities

data class Action<T : Any>(
    val uuid: String,
    val type: String,
    val targetUserId: Long,
    val data: T
)
