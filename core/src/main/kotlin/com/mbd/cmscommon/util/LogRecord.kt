package com.mbd.cmscommon.util

import java.util.UUID

/**
 * One central-logging entry. [logId] is client-generated so a record can be safely re-uploaded
 * after a failed flush without creating a duplicate row (upsert/ignore-on-conflict on the PK).
 */
data class LogRecord(
    val logId: String = UUID.randomUUID().toString(),
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
