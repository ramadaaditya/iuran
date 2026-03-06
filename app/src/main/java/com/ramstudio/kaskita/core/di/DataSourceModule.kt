package com.ramstudio.kaskita.core.di

import com.ramstudio.kaskita.data.datasource.remote.CommunityRemoteDataSource
import com.ramstudio.kaskita.data.datasource.remote.CommunityRemoteDataSourceImpl
import com.ramstudio.kaskita.data.datasource.remote.TransactionRemoteDataSource
import com.ramstudio.kaskita.data.datasource.remote.TransactionRemoteDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {
    @Binds
    abstract fun bindCommunityRemoteDataSource(
        impl: CommunityRemoteDataSourceImpl
    ): CommunityRemoteDataSource

    @Binds
    abstract fun bindTransactionRemoteDataSource(
        impl: TransactionRemoteDataSourceImpl
    ): TransactionRemoteDataSource
}
