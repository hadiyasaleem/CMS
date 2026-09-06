package com.mbd.cmscommon.data.remote.dto

import kotlinx.serialization.Serializable

/** Maps to the append-only `app_logs` table. See that migration for the RLS/shape rationale. */
@Serializable
data class AppLogDto(
    val logId: String,
    val occurredAt: String,
    val severity: String,
    val kind: String? = null,
    val tag: String? = null,
    val message: String,
    val stackTrace: String? = null,
    val accountEmail: String? = null,
    val appId: String? = null,
    val appVersion: String? = null,
    val platform: String? = null,
    val deviceInfo: String? = null,
)
