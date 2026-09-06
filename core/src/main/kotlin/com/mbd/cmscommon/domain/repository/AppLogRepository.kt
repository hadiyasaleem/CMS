package com.mbd.cmscommon.domain.repository

/**
 * Flushes the local Room buffer of critical/crash log records (written by `RoomLogSink`) to the
 * Supabase `app_logs` table. Called from `AdminDataBootstrapper.refreshAll()` during the normal
 * sync cycle — there is no dedicated schedule for it.
 */
interface AppLogRepository {
    suspend fun flush()
}
