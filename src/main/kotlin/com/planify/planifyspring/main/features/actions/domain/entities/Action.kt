package com.planify.planifyspring.main.features.actions.domain.entities

data class Action(
    val uuid: String,
    val type: String,
    val data: Any
)
