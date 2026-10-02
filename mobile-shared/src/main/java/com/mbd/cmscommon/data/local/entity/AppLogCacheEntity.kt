package com.mbd.cmscommon.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Local cache of server `app_logs` rows for the admin app's "App Logs" viewer, kept distinct from
 * [AppLogEntity] (this device's own outbox of unflushed records): outbox rows are deleted once
 * uploaded, but these must persist so the viewer works offline.
 */
@Entity(tableName = "app_log_cache", indices = [Index("occurredAtMillis")])
data class AppLogCacheEntity(
    @PrimaryKey val logId: String,
    val occurredAtMillis: Long,
    val severity: String,
    val kind: String?,
    val tag: String?,
    val message: String,
    val stackTrace: String?,
    val accountEmail: String?,
    val appId: String?,
    val appVersion: String?,
    val platform: String?,
    val deviceInfo: String?,
)
