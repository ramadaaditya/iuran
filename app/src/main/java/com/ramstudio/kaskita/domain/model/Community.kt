package com.ramstudio.kaskita.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Community(
    val id: String? = null,
    val name: String,
    val description: String,
    val code: String,
    @SerialName("created_by")
    val createdBy: String? = null
)

@Serializable
data class JoinResponse(
    val success: Boolean,
    val message: String
)

@Serializable
data class CommunityMember(
    @SerialName("community_id")
    val communityId: String,
    @SerialName("user_id")
    val userId: String,
    val role: String
)