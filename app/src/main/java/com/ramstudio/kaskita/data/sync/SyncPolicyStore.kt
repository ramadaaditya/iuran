package com.ramstudio.kaskita.data.sync

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncPolicyStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("sync_policy", Context.MODE_PRIVATE)

    fun shouldSync(key: String, maxAgeMillis: Long): Boolean {
        val lastSync = prefs.getLong(key, 0L)
        val now = System.currentTimeMillis()
        return now - lastSync >= maxAgeMillis
    }

    fun markSynced(key: String) {
        prefs.edit().putLong(key, System.currentTimeMillis()).apply()
    }
}
