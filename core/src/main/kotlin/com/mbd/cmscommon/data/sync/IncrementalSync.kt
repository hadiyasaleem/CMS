package com.mbd.cmscommon.data.sync

import com.mbd.cmscommon.data.remote.PgTime
import java.time.Instant
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

const val DEFAULT_DELTA_PAGE_SIZE = 500L

/**
 * Same end result as a plain `while (true) { fetch a page; apply it; stop once it's short }` loop, but
 * up to [concurrency] pages are ever in flight at once instead of exactly one -- page N+1's network
 * round trip overlaps page N's instead of waiting for it to finish first, so a large table's sync takes
 * roughly (page count / concurrency) round trips' worth of wall-clock time instead of (page count).
 *
 * [onPage] still runs exactly once per non-empty page, strictly in ascending offset order, and a page
 * beyond the first short one is never fetched -- every existing per-page side effect (a DAO upsert, a
 * running max-updated-at) behaves identically to the sequential loop this replaces; only the network
 * waiting is overlapped; the fetch/apply order is not.
 */
suspend fun <T> fetchPagesConcurrently(
    pageSize: Long,
    concurrency: Int = 4,
    fetchPage: suspend (from: Long, to: Long) -> List<T>,
    onPage: suspend (List<T>) -> Unit,
) = coroutineScope {
    var nextOffset = 0L
    val inFlight = ArrayDeque<Deferred<List<T>>>()
    fun launchNext() {
        val offset = nextOffset
        inFlight.addLast(async { fetchPage(offset, offset + pageSize - 1) })
        nextOffset += pageSize
    }
    repeat(concurrency) { launchNext() }

    while (inFlight.isNotEmpty()) {
        val page = inFlight.removeFirst().await()
        if (page.isEmpty()) continue
        onPage(page)
        // Only keep launching while pages keep coming back full -- a short page is the standard
        // "that was the last one" signal, so nothing past it is ever requested.
        if (page.size.toLong() == pageSize) launchNext()
    }
}

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
    val delta = mutableListOf<T>()

    fetchPagesConcurrently(
        pageSize = pageSize,
        fetchPage = { from, to -> fetchPage(since, from, to) },
    ) { page ->
        delta += page
        for (row in page) {
            val candidate = updatedAtOf(row) ?: continue
            if (PgTime.parseOrEpoch(candidate) > PgTime.parseOrEpoch(maxUpdatedAt)) {
                maxUpdatedAt = candidate
            }
        }
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