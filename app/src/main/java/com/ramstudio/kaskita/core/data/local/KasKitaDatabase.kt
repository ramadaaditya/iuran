package com.ramstudio.kaskita.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ramstudio.kaskita.core.data.local.dao.CommunityDao
import com.ramstudio.kaskita.core.data.local.dao.TransactionDao
import com.ramstudio.kaskita.core.data.local.entity.CommunityEntity
import com.ramstudio.kaskita.core.data.local.entity.MemberEntity
import com.ramstudio.kaskita.core.data.local.entity.TransactionEntity


@Database(
    entities = [
        CommunityEntity::class,
        MemberEntity::class,
        TransactionEntity::class,
    ],
    version = 3,
    exportSchema = false
)
abstract class KasKitaDatabase : RoomDatabase() {
    abstract fun communityDao(): CommunityDao
    abstract fun transactionDao(): TransactionDao
}
