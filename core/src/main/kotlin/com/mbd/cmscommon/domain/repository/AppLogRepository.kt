package com.mbd.cmscommon.domain.repository

import com.mbd.cmscommon.domain.model.AppLogRecord
import com.mbd.cmscommon.domain.model.AppLogStatus
import kotlinx.coroutines.flow.Flow

/**
 * Flushes the local Room buffer of critical/crash log records (written by `RoomLogSink`) to the
 * Supabase `app_logs` table, and -- separately -- lets the admin app pull that same table back down
 * for its own "App Logs" viewer, cached locally so it's readable offline. [flush] is called from
 * `AdminDataBootstrapper.refreshAll()` during the normal sync cycle; so is [sync], which downloads
 * into its own local cache distinct from the upload buffer (that buffer's rows are deleted once
 * flushed, but viewer rows must stick around to be read offline).
 */
interface AppLogRepository {
    suspend fun flush()

    /** Cached server log rows, newest first, for the admin log viewer. Only admins see any rows (RLS). */
    fun observeLogs(): Flow<List<AppLogRecord>>

    /** Downloads new rows from `app_logs` into the local viewer cache. */
    suspend fun sync()

    /** Sets [logId]'s triage status, admin-only (enforced server-side by `update_app_log_status`). */
    suspend fun updateStatus(logId: String, status: AppLogStatus)

    /**
     * Soft-deletes every currently-visible server log row, admin-only (enforced server-side by
     * `delete_all_app_logs`). Rows stay in `app_logs` for audit/history -- they just stop matching
     * `sel_app_logs` -- so this also clears the local viewer cache to match.
     */
    suspend fun deleteAll()
}
