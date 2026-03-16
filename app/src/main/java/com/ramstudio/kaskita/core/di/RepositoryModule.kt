package com.ramstudio.kaskita.core.di

import com.ramstudio.kaskita.core.data.repository.TransactionRepositoryImpl
import com.ramstudio.kaskita.core.data.repository.offline.OfflineFirstCommunityRepository
import com.ramstudio.kaskita.core.domain.repository.AuthRepository
import com.ramstudio.kaskita.core.domain.repository.CommunityRepository
import com.ramstudio.kaskita.core.domain.repository.TransactionRepository
import com.ramstudio.kaskita.core.utils.AuthRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindTransactionRepository(
        impl: TransactionRepositoryImpl
    ): TransactionRepository

    @Binds
    abstract fun bindCommunityRepository(
        impl: OfflineFirstCommunityRepository
    ): CommunityRepository

    @Binds
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository
}
