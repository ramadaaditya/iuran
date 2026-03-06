package com.ramstudio.kaskita.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "communities")
data class CommunityEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val code: String,
    val createdBy: String?,
    val balance: Double,
    val membersCount: Int,
    val updatedAt: Long,
)
