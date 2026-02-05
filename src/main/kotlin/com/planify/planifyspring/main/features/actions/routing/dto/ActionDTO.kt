package com.planify.planifyspring.main.features.actions.routing.dto

import com.planify.planifyspring.main.features.actions.domain.entities.Action

data class ActionDTO(
    val uuid: String,
    val type: String,
    val data: Any?
) {
    companion object {
        fun fromEntity(entity: Action): ActionDTO {
            return ActionDTO(
                uuid = entity.uuid,
                type = entity.type,
                data = entity.data
            )
        }
    }
}
