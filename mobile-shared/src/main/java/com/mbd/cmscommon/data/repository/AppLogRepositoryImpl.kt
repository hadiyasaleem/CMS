package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.data.remote.dto.AppLogDto
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import com.mbd.cmscommon.data.local.dao.AppLogDao
import com.mbd.cmscommon.data.mapper.AppLogMapper
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.domain.repository.AppLogRepository
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Inject

class AppLogRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val appLogDao: AppLogDao,
) : AppLogRepository {

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
    }
}
