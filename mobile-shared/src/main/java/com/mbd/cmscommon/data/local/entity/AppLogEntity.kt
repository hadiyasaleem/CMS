package com.mbd.cmscommon.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Local buffer for [com.mbd.cmscommon.util.LogRecord]s awaiting upload to the Supabase `app_logs`
 * table. `logId` is the client-generated UUID from the record itself, so a record that fails to
 * flush and is re-queued never duplicates. Append-only; rows are removed once uploaded (or once
 * [com.mbd.cmscommon.data.local.dao.AppLogDao.trimOldest] evicts them to cap local storage).
 */
@Entity(tableName = "app_logs", indices = [Index("occurredAtMillis")])
data class AppLogEntity(
    @PrimaryKey val logId: String,
    val occurredAtMillis: Long,
    val severity: String,
    val kind: String,
    val tag: String,
    val message: String,
    val stackTrace: String?,
    val accountEmail: String?,
    val appId: String?,
    val appVersion: String?,
    val platform: String?,
    val deviceInfo: String?,
)
