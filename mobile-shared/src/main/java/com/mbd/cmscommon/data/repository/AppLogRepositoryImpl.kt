package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.data.remote.dto.AppLogDto
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import com.mbd.cmscommon.data.local.dao.AppLogCacheDao
import com.mbd.cmscommon.data.local.dao.AppLogDao
import com.mbd.cmscommon.data.mapper.AppLogMapper
import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.sync.SyncCheckpoint
import com.mbd.cmscommon.data.sync.SyncCheckpointDefaults
import com.mbd.cmscommon.data.sync.SyncCheckpointStore
import com.mbd.cmscommon.data.sync.fetchPagesConcurrently
import com.mbd.cmscommon.data.sync.maxRemoteUpdatedAt
import com.mbd.cmscommon.domain.model.AppLogRecord
import com.mbd.cmscommon.domain.model.AppLogStatus
import com.mbd.cmscommon.domain.repository.AppLogRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AppLogRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val appLogDao: AppLogDao,
    private val appLogCacheDao: AppLogCacheDao,
    private val checkpointStore: SyncCheckpointStore,
    private val sessionManager: SessionManager,
) : AppLogRepository {

    override fun observeLogs(): Flow<List<AppLogRecord>> =
        appLogCacheDao.observeRecent(MAX_VIEW_ROWS).map { rows -> rows.map(AppLogMapper::cacheEntityToDomain) }

    /**
     * Downloads new rows from `app_logs` into the viewer cache. RLS (see the `app_logs` migration)
     * only lets admins select any rows at all, so this is a harmless no-op for teacher/student
     * accounts rather than something that needs its own role check here.
     */
    override suspend fun sync() {
        val ownerKey = SyncCheckpointDefaults.ownerKey(sessionManager.accountKey ?: "anonymous-local")
        val scopeKey = SyncCheckpointDefaults.globalScope()
        val since = checkpointStore.get(ownerKey, SupabaseTables.APP_LOGS, scopeKey)?.lastUpdatedAt ?: SyncCheckpointDefaults.EPOCH
        var maxOccurredAt = since

        fetchPagesConcurrently(
            pageSize = PAGE_SIZE,
            fetchPage = { from, to ->
                postgrest.from(SupabaseTables.APP_LOGS).select {
                    filter { gt("occurred_at", since) }
                    order("occurred_at", Order.ASCENDING)
                    range(from, to)
                }.decodeList<AppLogDto>()
            },
        ) { page ->
            appLogCacheDao.upsertAll(page.map(AppLogMapper::dtoToCacheEntity))
            maxOccurredAt = page.maxRemoteUpdatedAt(maxOccurredAt) { it.occurredAt }
        }

        appLogCacheDao.trimOldest(MAX_VIEW_ROWS)
        checkpointStore.upsert(SyncCheckpoint(ownerKey, SupabaseTables.APP_LOGS, scopeKey, maxOccurredAt, PgTime.format(Instant.now()) ?: since))
    }

    /** `app_logs` has no UPDATE policy (see its migration), so this goes through a definer function
     * that also re-checks admin-ness server-side rather than trusting the caller's role. */
    override suspend fun updateStatus(logId: String, status: AppLogStatus) {
        postgrest.rpc(
            SupabaseTables.RPC_UPDATE_APP_LOG_STATUS,
            buildJsonObject {
                put("p_log_id", logId)
                put("p_status", status.name)
            },
        )
        appLogCacheDao.updateStatus(logId, status.name)
    }

    /** Same reasoning as [updateStatus]: no UPDATE policy on `app_logs`, so this goes through a
     * definer function that re-checks admin-ness server-side. The rows stay in `app_logs` (soft
     * delete), but the viewer's local cache is cleared since nothing it holds is visible any more. */
    override suspend fun deleteAll() {
        postgrest.rpc(SupabaseTables.RPC_DELETE_ALL_APP_LOGS, buildJsonObject {})
        appLogCacheDao.deleteAll()
    }

    /**
     * Uploads buffered log rows, oldest first, then trims local storage. Deliberately swallows
     * every failure with no [com.mbd.cmscommon.util.CmsLog] call of its own — logging a failure to
     * upload logs would recurse straight back into the system this repository serves. A row that
     * fails to upload simply stays in Room and is retried on the next sync.
     */
    override suspend fun flush() {
        runCatching {
            var batches = 0
            while (batches < MAX_BATCHES_PER_FLUSH) {
                val batch = appLogDao.pendingBatch(BATCH_SIZE)
                if (batch.isEmpty()) break
                val dtos = batch.map(AppLogMapper::entityToDto)
                // Idempotent on the PK (client-generated logId): a row re-queued after a delete that failed to commit
                // locally must not be refused on retry. `app_logs` has no UPDATE policy, so an upsert's conflict path
                // hit RLS and blocked the whole batch; the function inserts with `on conflict do nothing` instead.
                postgrest.rpc(SupabaseTables.RPC_INGEST_APP_LOGS, buildJsonObject { put("p_rows", dtos.toJsonRows()) })
                appLogDao.deleteByIds(batch.map { it.logId })
                batches++
                if (batch.size < BATCH_SIZE) break
            }
            appLogDao.trimOldest(MAX_LOCAL_ROWS)
        }
    }

    /** The batch as the JSON array `ingest_app_logs` expects (column names spelled out, independent of the client's naming strategy). */
    private fun List<AppLogDto>.toJsonRows() = buildJsonArray {
        forEach { log ->
            add(
                buildJsonObject {
                    put("log_id", log.logId)
                    put("occurred_at", log.occurredAt)
                    put("severity", log.severity)
                    put("message", log.message)
                    log.kind?.let { put("kind", it) }
                    log.tag?.let { put("tag", it) }
                    log.stackTrace?.let { put("stack_trace", it) }
                    log.accountEmail?.let { put("account_email", it) }
                    log.appId?.let { put("app_id", it) }
                    log.appVersion?.let { put("app_version", it) }
                    log.platform?.let { put("platform", it) }
                    log.deviceInfo?.let { put("device_info", it) }
                },
            )
        }
    }

    private companion object {
        const val BATCH_SIZE = 100
        const val MAX_BATCHES_PER_FLUSH = 5
        const val MAX_LOCAL_ROWS = 500
        const val PAGE_SIZE = 500L
        const val MAX_VIEW_ROWS = 1000
    }
}
