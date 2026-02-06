package com.planify.planifyspring.main.features.actions.domain.entities

data class Action(
    val id: String,
    val checked: Boolean = false,
    val type: String,
    val data: Any
)
