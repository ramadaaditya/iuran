package com.ramstudio.kaskita.data.local

import com.ramstudio.kaskita.data.local.entity.CommunityEntity
import com.ramstudio.kaskita.data.local.entity.MemberEntity
import com.ramstudio.kaskita.data.local.entity.TransactionEntity
import com.ramstudio.kaskita.domain.model.Community
import com.ramstudio.kaskita.domain.model.Transaction
import com.ramstudio.kaskita.domain.model.TransactionCategory
import com.ramstudio.kaskita.domain.model.TransactionStatus
import com.ramstudio.kaskita.domain.model.User

fun Community.toEntity(now: Long): CommunityEntity? {
    val communityId = id ?: return null
    return CommunityEntity(
        id = communityId,
        name = name,
        description = description,
        code = code,
        createdBy = createdBy,
        balance = balance,
        membersCount = membersCount,
        updatedAt = now,
    )
}

fun CommunityEntity.toDomain(memberCountOverride: Int? = null): Community {
    return Community(
        id = id,
        name = name,
        description = description,
        code = code,
        createdBy = createdBy,
        balance = balance,
        membersCount = memberCountOverride ?: membersCount,
    )
}

fun User.toMemberEntity(communityId: String, now: Long): MemberEntity {
    return MemberEntity(
        communityId = communityId,
        userId = id,
        name = name,
        role = role,
        initial = initial,
        email = email,
        updatedAt = now,
    )
}

fun MemberEntity.toDomain(): User {
    return User(
        id = userId,
        name = name,
        role = role,
        initial = initial,
        email = email,
    )
}

fun Transaction.toEntity(now: Long): TransactionEntity {
    return TransactionEntity(
        id = id,
        communityId = communityId,
        userId = userId,
        amount = amount,
        description = description,
        proofUrl = proofUrl,
        createdAt = createdAt,
        type = type.name,
        status = status.name,
        updatedAt = now,
    )
}

fun TransactionEntity.toDomain(): Transaction {
    return Transaction(
        id = id,
        communityId = communityId,
        userId = userId,
        amount = amount,
        description = description,
        proofUrl = proofUrl,
        createdAt = createdAt,
        type = runCatching { TransactionCategory.valueOf(type) }.getOrDefault(TransactionCategory.INCOME),
        status = runCatching { TransactionStatus.valueOf(status) }.getOrDefault(TransactionStatus.PENDING),
    )
}
