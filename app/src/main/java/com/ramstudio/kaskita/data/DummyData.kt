package com.ramstudio.kaskita.data

import androidx.compose.ui.graphics.Color
import com.ramstudio.kaskita.domain.model.Community
import com.ramstudio.kaskita.domain.model.Transaction
import com.ramstudio.kaskita.domain.model.TransactionCategory
import com.ramstudio.kaskita.domain.model.TransactionStatus

object DummyData {
    val communities =
        listOf(
            Community(
                id = "komunitas_001",
                name = "Garden Club",
                description = "Komunitas berkebun warga blok A",
                code = "GARDEN-A",
                createdBy = "user_123",
                balance = 14250.00,
                themeColor = Color(0xFF00BFA5)
            ),
            Community(
                id = "komunitas_002",
                name = "Reading Circle",
                description = "Klub baca buku bulanan",
                code = "READ-CC",
                createdBy = "user_888",
                balance = 850.50,
                themeColor = Color(0xFF00BFA5)
            ),
            Community(
                id = "komunitas_003",
                name = "HOA Fund",
                description = "Kas paguyuban perumahan",
                code = "HOA-P",
                createdBy = "user_123",
                balance = 54000.00,
                themeColor = Color(0xFF00BFA5)
            )
        )

    val transactions = listOf(
        Transaction(
            id = "1",
            communityId = "community_1",
            userId = "user_1",
            amount = 42000.0,
            description = "Community Dinner",
            createdAt = System.currentTimeMillis(),
            type = TransactionCategory.EXPENSE,
            status = TransactionStatus.SUCCESS
        ),

        Transaction(
            id = "2",
            communityId = "community_1",
            userId = "user_2",
            amount = 1200000.0,
            description = "Monthly Member Contribution",
            createdAt = System.currentTimeMillis(),
            type = TransactionCategory.INCOME,
            status = TransactionStatus.SUCCESS
        ),

        Transaction(
            id = "3",
            communityId = "community_1",
            userId = "user_3",
            amount = 280000.0,
            description = "Pipe Repair Maintenance",
            createdAt = System.currentTimeMillis(),
            type = TransactionCategory.EXPENSE,
            status = TransactionStatus.PENDING
        )
    )
}