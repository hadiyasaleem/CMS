package com.mbd.cmscommon.data.repository

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
                // Upsert on the PK (client-generated logId): a row re-queued after a delete that
                // failed to commit locally must not throw a duplicate-key error on retry.
                postgrest.from(SupabaseTables.APP_LOGS).upsert(dtos) { onConflict = "log_id" }
                appLogDao.deleteByIds(batch.map { it.logId })
                batches++
                if (batch.size < BATCH_SIZE) break
            }
            appLogDao.trimOldest(MAX_LOCAL_ROWS)
        }
    }

    private companion object {
        const val BATCH_SIZE = 100
        const val MAX_BATCHES_PER_FLUSH = 5
        const val MAX_LOCAL_ROWS = 500
    }
}
