package com.ramstudio.kaskita.core.di

import com.ramstudio.kaskita.core.utils.AuthRepositoryImpl
import com.ramstudio.kaskita.data.repository.RemoteCommunityRepository
import com.ramstudio.kaskita.data.repository.RemoteTransactionRepository
import com.ramstudio.kaskita.domain.repository.AuthRepository
import com.ramstudio.kaskita.domain.repository.ICommunityRepository
import com.ramstudio.kaskita.domain.repository.ITransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
//    @Binds
//    abstract fun bindCommunityRepository(
//        impl: MockCommunityRepository
//    ): ICommunityRepository

//    @Binds
//    abstract fun bindTransactionRepository(
//        impl: MockTransactionRepository
//    ): ITransactionRepository

    @Binds
    abstract fun bindTransactionRepository(
        impl: RemoteTransactionRepository
    ): ITransactionRepository

    @Binds
    abstract fun bindCommunityRepository(
        impl: RemoteCommunityRepository
    ): ICommunityRepository

    @Binds
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository
}