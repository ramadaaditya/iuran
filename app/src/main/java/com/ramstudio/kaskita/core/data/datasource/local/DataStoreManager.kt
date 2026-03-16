package com.ramstudio.kaskita.core.data.datasource.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton


val Context.dataStore by preferencesDataStore(name = "kaskita_pref")

@Singleton
class DataStoreManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    companion object {
        private const val COMMUNITY_KEY_PREFIX = "community_id_"
    }

    private fun communityKey(userId: String) =
        stringPreferencesKey("$COMMUNITY_KEY_PREFIX$userId")

    fun observeSelectedCommunityId(userId: String): Flow<String?> =
        context.dataStore.data.map { pref ->
            pref[communityKey(userId)]?.takeIf { it.isNotBlank() }
        }

    suspend fun saveSelectedCommunityId(userId: String, communityId: String?) {
        context.dataStore.edit { pref ->
            val key = communityKey(userId)
            val normalized = communityId?.takeIf { it.isNotBlank() }
            pref[key] = normalized.orEmpty()
        }
    }

}