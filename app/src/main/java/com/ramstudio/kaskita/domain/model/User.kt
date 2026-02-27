package com.ramstudio.kaskita.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class User(
    val id: String,
    val name: String,
    val role: String,
    val initial: String,
    val email: String? = ""
)


@Serializable
data class ProfileDto(
    val id: String,
    @SerialName("full_name")
    val fullName: String? = null,
    @SerialName("avatar_url")
    val avatarUrl: String? = null
)