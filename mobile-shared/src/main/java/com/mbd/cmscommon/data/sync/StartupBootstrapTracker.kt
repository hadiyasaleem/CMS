package com.mbd.cmscommon.data.sync

import com.mbd.cmscommon.data.local.dao.SyncStateDao
import com.mbd.cmscommon.data.local.entity.SyncStateEntity
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StartupBootstrapTracker @Inject constructor(
    private val syncStateDao: SyncStateDao,
) {
    suspend fun isComplete(scope: String, accountKey: String): Boolean =
        syncStateDao.getLastSyncedAt(key(scope, accountKey)) != null

    suspend fun markComplete(scope: String, accountKey: String) {
        syncStateDao.upsert(SyncStateEntity(key(scope, accountKey), System.currentTimeMillis()))
    }

    private fun key(scope: String, accountKey: String): String =
        "startup-bootstrap-v1:$scope:${accountKey.trim().lowercase(Locale.ROOT)}"

    companion object {
        const val REFERENCE_DATA = "reference-data"
        const val ADMIN_DATA = "admin-data"
        const val STUDENT_SESSION = "student-session"
    }
}
