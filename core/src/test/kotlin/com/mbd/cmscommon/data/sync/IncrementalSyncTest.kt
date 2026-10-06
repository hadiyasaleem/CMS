package com.mbd.cmscommon.data.sync

import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class IncrementalSyncTest {

    private data class Row(val id: String, val updatedAt: String, val deleted: Boolean = false)

    @Test
    fun fetchesEveryPageFromInclusiveCheckpointAndAdvancesToMaximumTimestamp() = runBlocking {
        val initial = checkpoint("2026-08-30T10:00:00Z")
        val store = FakeCheckpointStore(initial)
        val calls = mutableListOf<Triple<String, Long, Long>>()

        val rows = fetchIncrementalDelta(
            checkpointStore = store,
            ownerKey = OWNER,
            tableName = TABLE,
            scopeKey = SCOPE,
            updatedAtOf = Row::updatedAt,
            pageSize = 2,
            applyDelta = { },
        ) { since, from, to ->
            calls += Triple(since, from, to)
            when (from) {
                0L -> listOf(
                    Row("a", "2026-08-30T10:00:00Z"),
                    Row("b", "2026-08-30T10:01:00Z"),
                )
                2L -> listOf(Row("c", "2026-08-30T10:02:00Z"))
                else -> emptyList()
            }
        }

        assertEquals(listOf("a", "b", "c"), rows.map(Row::id))
        // Pages fetch concurrently now, so a few harmless speculative requests past the real data are
        // expected (and not asserted here) -- what matters is that both real pages were requested.
        assertTrue(calls.contains(Triple("2026-08-30T10:00:00Z", 0L, 1L)))
        assertTrue(calls.contains(Triple("2026-08-30T10:00:00Z", 2L, 3L)))
        assertEquals("2026-08-30T10:02:00Z", store.value?.lastUpdatedAt)
        assertEquals(1, store.upsertCount)
    }

    @Test
    fun doesNotAdvanceCheckpointWhenAnyPageFails() = runBlocking {
        val initial = checkpoint("2026-08-30T10:00:00Z")
        val store = FakeCheckpointStore(initial)

        try {
            fetchIncrementalDelta(
                checkpointStore = store,
                ownerKey = OWNER,
                tableName = TABLE,
                scopeKey = SCOPE,
                updatedAtOf = Row::updatedAt,
                pageSize = 2,
                applyDelta = { },
            ) { _, from, _ ->
                if (from == 0L) {
                    listOf(
                        Row("a", "2026-08-30T10:01:00Z"),
                        Row("b", "2026-08-30T10:02:00Z"),
                    )
                } else {
                    error("page failed")
                }
            }
            fail("Expected the second page to fail")
        } catch (expected: IllegalStateException) {
            assertEquals("page failed", expected.message)
        }

        assertSame(initial, store.value)
        assertEquals(0, store.upsertCount)
    }

    @Test
    fun doesNotAdvanceCheckpointWhenApplyingDeltaFails() = runBlocking {
        val initial = checkpoint("2026-08-30T10:00:00Z")
        val store = FakeCheckpointStore(initial)

        try {
            fetchIncrementalDelta(
                checkpointStore = store,
                ownerKey = OWNER,
                tableName = TABLE,
                scopeKey = SCOPE,
                updatedAtOf = Row::updatedAt,
                applyDelta = { error("local cache write failed") },
            ) { _, _, _ -> listOf(Row("a", "2026-08-30T10:01:00Z")) }
            fail("Expected the local cache write to fail")
        } catch (expected: IllegalStateException) {
            assertEquals("local cache write failed", expected.message)
        }

        assertSame(initial, store.value)
        assertEquals(0, store.upsertCount)
    }
    @Test
    fun mergesUpsertsAndTombstonesByStableKey() {
        val existing = listOf(
            Row("a", "2026-08-30T10:00:00Z"),
            Row("b", "2026-08-30T10:00:00Z"),
        )
        val delta = listOf(
            Row("a", "2026-08-30T10:01:00Z", deleted = true),
            Row("b", "2026-08-30T10:02:00Z"),
            Row("c", "2026-08-30T10:03:00Z"),
        )

        val merged = mergeIncrementalDelta(existing, delta, Row::id, Row::deleted)

        assertEquals(listOf("b", "c"), merged.map(Row::id))
        assertEquals("2026-08-30T10:02:00Z", merged.first().updatedAt)
    }

    @Test
    fun concurrentPagingAppliesEveryPageInOffsetOrderEvenWhenLaterPagesFinishFirst() = runBlocking {
        val applied = mutableListOf<Long>()
        val maxInFlight = AtomicInteger(0)
        val inFlight = AtomicInteger(0)

        fetchPagesConcurrently<Int>(pageSize = 2, concurrency = 3, fetchPage = { from, _ ->
            inFlight.incrementAndGet().also { maxInFlight.updateAndGet { m -> maxOf(m, it) } }
            // Page 0 is the slowest to resolve -- if results were applied in completion order
            // instead of offset order, page 2's row would land before page 0's.
            delay(if (from == 0L) 30L else 5L)
            inFlight.decrementAndGet()
            when (from) {
                0L -> listOf(1, 2)
                2L -> listOf(3, 4)
                4L -> listOf(5)
                else -> emptyList()
            }
        }, onPage = { page -> applied += from(page) })

        assertEquals(listOf(1L, 2L, 3L, 4L, 5L), applied)
        assertTrue("at least 2 pages should have been in flight at once", maxInFlight.get() >= 2)
    }

    @Test
    fun concurrentPagingStopsLaunchingOnceAPageComesBackShort() = runBlocking {
        val fetched = mutableListOf<Long>()

        // concurrency=2 fires offsets {0, 2} up front regardless of how much data there is -- that's
        // the point of windowing. A full page only ever launches ONE more (its own next offset), so
        // offset 0 coming back full launches offset 4; offset 2 coming back short launches nothing.
        // Offset 6 would only ever be launched if offset 4 *also* came back full, which it must not.
        fetchPagesConcurrently<Int>(pageSize = 2, concurrency = 2, fetchPage = { from, _ ->
            fetched += from
            when (from) {
                0L -> listOf(1, 2)
                2L -> listOf(3) // short page: the last one with real data
                4L -> emptyList()
                else -> error("offset $from should never be fetched")
            }
        }, onPage = {})

        assertEquals(listOf(0L, 2L, 4L), fetched.sorted())
    }

    private fun from(page: List<Int>) = page.map { it.toLong() }

    private fun checkpoint(lastUpdatedAt: String) = SyncCheckpoint(
        ownerKey = OWNER,
        tableName = TABLE,
        scopeKey = SCOPE,
        lastUpdatedAt = lastUpdatedAt,
        lastSuccessfulSyncAt = lastUpdatedAt,
    )

    private class FakeCheckpointStore(initial: SyncCheckpoint? = null) : SyncCheckpointStore {
        var value: SyncCheckpoint? = initial
        var upsertCount: Int = 0

        override suspend fun get(ownerKey: String, tableName: String, scopeKey: String): SyncCheckpoint? = value

        override suspend fun upsert(checkpoint: SyncCheckpoint) {
            value = checkpoint
            upsertCount += 1
        }

        override suspend fun clear(ownerKey: String, tableName: String, scopeKey: String) {
            value = null
        }
    }

    private companion object {
        const val OWNER = "admin@example.com"
        const val TABLE = "departments"
        const val SCOPE = "global"
    }
}
