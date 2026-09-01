package com.mbd.cmscommon.data.sync

import com.mbd.cmscommon.data.remote.PgTime
import java.time.Instant

const val DEFAULT_DELTA_PAGE_SIZE = 500L

/**
 * Downloads one table/scope delta, persists it through [applyDelta], then advances its checkpoint.
 * Inclusive high-water marks intentionally replay rows that share the boundary timestamp; local
 * stores merge by stable key, making that replay safe while avoiding missed equal-timestamp rows.
 */
suspend fun <T> fetchIncrementalDelta(
    checkpointStore: SyncCheckpointStore,
    ownerKey: String,
    tableName: String,
    scopeKey: String,
    updatedAtOf: (T) -> String?,
    pageSize: Long = DEFAULT_DELTA_PAGE_SIZE,
    applyDelta: suspend (List<T>) -> Unit = {},
    fetchPage: suspend (since: String, from: Long, to: Long) -> List<T>,
): List<T> {
    val checkpoint = checkpointStore.get(ownerKey, tableName, scopeKey)
    val since = checkpoint?.lastUpdatedAt ?: SyncCheckpointDefaults.EPOCH
    var maxUpdatedAt = since
    var offset = 0L
    val delta = mutableListOf<T>()

    while (true) {
        val page = fetchPage(since, offset, offset + pageSize - 1)
        if (page.isEmpty()) break
        delta += page
        for (row in page) {
            val candidate = updatedAtOf(row) ?: continue
            if (PgTime.parseOrEpoch(candidate) > PgTime.parseOrEpoch(maxUpdatedAt)) {
                maxUpdatedAt = candidate
            }
        }
        if (page.size < pageSize) break
        offset += pageSize
    }

    applyDelta(delta)
    checkpointStore.upsert(
        SyncCheckpoint(
            ownerKey = ownerKey,
            tableName = tableName,
            scopeKey = scopeKey,
            lastUpdatedAt = maxUpdatedAt,
            lastSuccessfulSyncAt = PgTime.format(Instant.now()) ?: since,
        ),
    )
    return delta
}

/** Applies upserts and tombstones without evicting rows outside the refreshed query scope. */
fun <T, K> mergeIncrementalDelta(
    existing: List<T>,
    delta: List<T>,
    keyOf: (T) -> K,
    isDeleted: (T) -> Boolean,
): List<T> {
    if (delta.isEmpty()) return existing
    val merged = existing.associateByTo(linkedMapOf(), keyOf)
    for (row in delta) {
        val key = keyOf(row)
        if (isDeleted(row)) merged.remove(key) else merged[key] = row
    }
    return merged.values.toList()
}