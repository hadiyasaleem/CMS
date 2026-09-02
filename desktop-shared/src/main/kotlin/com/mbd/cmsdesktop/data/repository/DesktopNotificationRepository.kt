package com.mbd.cmsdesktop.data.repository

import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.data.local.dao.NotificationDao
import com.mbd.cmscommon.data.repository.BaseNotificationRepository
import com.mbd.cmscommon.data.sync.SyncCheckpointStore
import com.mbd.cmsdesktop.data.local.dao.NotificationViewStateDao
import com.mbd.cmsdesktop.data.local.entity.NotificationViewStateEntity
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Desktop notification data, including its read marker, is persisted only in Room. */
class DesktopNotificationRepository @Inject constructor(
    postgrest: Postgrest,
    notificationDao: NotificationDao,
    private val viewStateDao: NotificationViewStateDao,
    checkpointStore: SyncCheckpointStore,
    sessionManager: SessionManager,
) : BaseNotificationRepository(postgrest, notificationDao, checkpointStore, sessionManager) {

    override fun observeLastViewedAt(): Flow<Long> = viewStateDao.observeLastViewedAt().map { it ?: 0L }

    override suspend fun recordViewedNow() {
        viewStateDao.upsert(NotificationViewStateEntity(lastViewedAt = System.currentTimeMillis()))
    }
}
