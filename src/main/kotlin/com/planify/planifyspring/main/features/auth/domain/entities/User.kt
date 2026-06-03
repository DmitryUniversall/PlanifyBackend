package com.planify.planifyspring.main.features.auth.domain.entities

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

data class User(
    val id: Long,
    val username: String,
    val email: String,
    val passwordHash: String,

    @get:JsonProperty("isActivated")  // TODO: CREATE DTO FOR THIS
    @param:JsonProperty("isActivated")
    val isActivated: Boolean = false
) : Serializable
