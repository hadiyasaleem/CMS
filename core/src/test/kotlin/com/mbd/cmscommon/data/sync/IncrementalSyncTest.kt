package com.mbd.cmscommon.data.sync

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
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
        assertEquals(
            listOf(
                Triple("2026-08-30T10:00:00Z", 0L, 1L),
                Triple("2026-08-30T10:00:00Z", 2L, 3L),
            ),
            calls,
        )
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
