package com.planify.planifyspring.main.features.actions.routing

import com.planify.planifyspring.main.features.actions.domain.entities.Action

data class ActionDTO<T : Any>(
    val uuid: String,
    val type: String,
    val targetUserId: Long,
    val data: T
) {
    companion object {
        fun <T : Any> fromEntity(entity: Action<T>): ActionDTO<T> {
            return ActionDTO(
                uuid = entity.uuid,
                type = entity.type,
                targetUserId = entity.targetUserId,
                data = entity.data
            )
        }
    }
}
