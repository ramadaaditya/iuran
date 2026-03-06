package com.ramstudio.kaskita.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "community_members",
    primaryKeys = ["communityId", "userId"],
    indices = [Index(value = ["communityId"])]
)
data class MemberEntity(
    val communityId: String,
    val userId: String,
    val name: String,
    val role: String,
    val initial: String,
    val email: String?,
    val updatedAt: Long,
)
