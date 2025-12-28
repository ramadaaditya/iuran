package com.ramstudio.kaskita.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule{
    @Provides
    @Singleton
    fun provideSupabaseClient() : SupabaseClient{
        return createSupabaseClient(
            supabaseUrl = "https://jipxvjzjtoysavdoskoi.supabase.co",
            supabaseKey = "sb_publishable_doZwpA-lIlXxB8-Yg71eHg_ItZV38M7"
        ) {
            install(Auth)
            install(Postgrest)
        }
    }
}