package com.ramstudio.kaskita.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["communityId"]),
        Index(value = ["createdAt"]),
    ]
)
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val communityId: String,
    val userId: String,
    val amount: Double,
    val description: String?,
    val createdAt: Long,
    val type: String,
    val status: String,
    val updatedAt: Long,
)
