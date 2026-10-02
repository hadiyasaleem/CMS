package com.mbd.cmscommon.domain.model

import java.time.Instant

/** One row from the server's `app_logs` table, as shown in the admin app's log viewer. */
data class AppLogRecord(
    val logId: String,
    val occurredAt: Instant,
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
