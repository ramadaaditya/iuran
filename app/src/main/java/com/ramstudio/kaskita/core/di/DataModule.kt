package com.ramstudio.kaskita.core.di

import android.content.Context
import androidx.room.Room
import com.ramstudio.kaskita.data.local.KasKitaDatabase
import com.ramstudio.kaskita.data.local.dao.CommunityDao
import com.ramstudio.kaskita.data.local.dao.TransactionDao
import com.ramstudio.kaskita.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.Storage
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = BuildConfig.PROJECT_URL,
            supabaseKey = BuildConfig.ANON_KEY
        ) {
            install(Auth)
            install(Postgrest)
            install(Storage)
        }
    }


    @Provides
    @Singleton
    fun providePostgrest(client: SupabaseClient): Postgrest {
        return client.postgrest
    }

    @Provides
    @Singleton
    fun provideAuth(client: SupabaseClient): Auth {
        return client.auth
    }

    @Provides
    @Singleton
    fun provideKasKitaDatabase(
        @ApplicationContext context: Context
    ): KasKitaDatabase {
        return Room.databaseBuilder(
            context,
            KasKitaDatabase::class.java,
            "kaskita.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideCommunityDao(database: KasKitaDatabase): CommunityDao {
        return database.communityDao()
    }

    @Provides
    fun provideTransactionDao(database: KasKitaDatabase): TransactionDao {
        return database.transactionDao()
    }

}
