package com.ramstudio.kaskita.core.di

import com.ramstudio.kaskita.data.repository.CommunityRepository
import com.ramstudio.kaskita.domain.repository.ICommunityRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindCommunityRepository(
        impl: CommunityRepository
    ): ICommunityRepository
}