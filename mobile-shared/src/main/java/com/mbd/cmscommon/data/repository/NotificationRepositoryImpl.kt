package com.mbd.cmscommon.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.data.local.dao.NotificationDao
import com.mbd.cmscommon.data.sync.SyncCheckpointStore
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val LAST_VIEWED_KEY = longPreferencesKey("last_notifications_viewed_at")

class NotificationRepositoryImpl @Inject constructor(
    postgrest: Postgrest,
    notificationDao: NotificationDao,
    private val dataStore: DataStore<Preferences>,
    checkpointStore: SyncCheckpointStore,
    sessionManager: SessionManager,
) : BaseNotificationRepository(postgrest, notificationDao, checkpointStore, sessionManager) {

    override fun observeLastViewedAt(): Flow<Long> = dataStore.data.map { it[LAST_VIEWED_KEY] ?: 0L }

    override suspend fun recordViewedNow() {
        dataStore.edit { it[LAST_VIEWED_KEY] = System.currentTimeMillis() }
    }
}
