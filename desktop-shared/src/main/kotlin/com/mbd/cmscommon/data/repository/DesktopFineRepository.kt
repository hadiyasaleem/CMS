package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.data.mapper.DesktopFineMapper
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.FineDto
import com.mbd.cmscommon.data.sync.SyncCheckpointDefaults
import com.mbd.cmscommon.data.sync.fetchIncrementalDelta
import com.mbd.cmscommon.data.sync.mergeIncrementalDelta
import com.mbd.cmscommon.domain.model.Fine
import com.mbd.cmscommon.domain.repository.FineRepository
import com.mbd.cmsdesktop.data.cache.DesktopBootstrapSnapshotStore
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton

/** Durable cache-first fines repository. */
@Singleton
class DesktopFineRepository @Inject constructor(
    private val postgrest: Postgrest,
    private val store: DesktopBootstrapSnapshotStore,
    private val sessionManager: SessionManager,
) : FineRepository {
    override suspend fun getFines(sessionId: String, rollNumber: String): List<Fine> =
        cachedRows().filter {
            it.sessionId == sessionId && it.rollNumber == rollNumber && !it.isDeleted
        }.map(DesktopFineMapper::dtoToDomain).sortedByDescending { it.issuedAt }

    override suspend fun sync(sessionId: String, rollNumber: String) {
        val delta = fetchIncrementalDelta(
            store,
            ownerKey(),
            SupabaseTables.FINES,
            SyncCheckpointDefaults.scoped("session" to sessionId, "roll" to rollNumber),
            FineDto::updatedAt,
        ) { since, from, to ->
            postgrest.from(SupabaseTables.FINES).select {
                filter {
                    eq("session_id", sessionId)
                    eq("roll_number", rollNumber)
                    gte("updated_at", since)
                }
                order("updated_at", Order.ASCENDING)
                range(from, to)
            }.decodeList()
        }
        writeMerged(delta)
    }

    override suspend fun issueFine(
        sessionId: String,
        rollNumber: String,
        category: String,
        amount: Double,
        reason: String,
        issuedBy: String,
    ) {
        val inserted = postgrest.from(SupabaseTables.FINES).insert(
            FineDto(
                sessionId = sessionId,
                rollNumber = rollNumber,
                category = category.ifBlank { "OTHER" },
                amount = amount,
                reason = reason.trim(),
                issuedBy = issuedBy,
            ),
        ) { select() }.decodeList<FineDto>()
        writeMerged(inserted)
    }

    override suspend fun deleteFine(id: String) {
        postgrest.from(SupabaseTables.FINES).update({ set("is_deleted", true) }) {
            filter { eq("id", id) }
        }
        store.writeRows(CACHE_FILE, FineDto.serializer(), cachedRows().filterNot { it.id == id })
    }

    private fun cachedRows() = store.readRows(CACHE_FILE, FineDto.serializer())

    private fun writeMerged(delta: List<FineDto>) {
        store.writeRows(CACHE_FILE, FineDto.serializer(), mergeIncrementalDelta(
            cachedRows(), delta, { it.id ?: "entity:${it.entityId}" }, FineDto::isDeleted,
        ))
    }

    private fun ownerKey() =
        sessionManager.accountKey ?: SyncCheckpointDefaults.ownerKey("anonymous-local")

    private companion object { const val CACHE_FILE = "fines.json" }
}
