package com.mbd.cmscommon.domain.model

import java.time.Instant

/** An admin's triage state for one [AppLogRecord], set from the App Logs screen. */
enum class AppLogStatus(val label: String) {
    NEW("New"),
    IN_PROGRESS("In progress"),
    FIXED("Fixed"),
}

/** [raw] as reported by the server; an unrecognised or missing value defaults to [AppLogStatus.NEW]
 * rather than failing the whole row, since status is metadata about the log, not the log itself. */
fun parseAppLogStatus(raw: String?): AppLogStatus = AppLogStatus.entries.find { it.name == raw } ?: AppLogStatus.NEW

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
    val status: AppLogStatus = AppLogStatus.NEW,
)
