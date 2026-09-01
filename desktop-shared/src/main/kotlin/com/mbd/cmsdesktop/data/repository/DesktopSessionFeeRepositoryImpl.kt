package com.mbd.cmsdesktop.data.repository

import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.data.mapper.DesktopSessionFeeMapper
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.SessionFeeDto
import com.mbd.cmscommon.data.remote.dto.SessionFeeHeadDto
import com.mbd.cmscommon.data.sync.SyncCheckpointDefaults
import com.mbd.cmscommon.data.sync.fetchIncrementalDelta
import com.mbd.cmscommon.data.sync.mergeIncrementalDelta
import com.mbd.cmscommon.domain.model.SessionFeeStructure
import com.mbd.cmscommon.domain.repository.SessionFeeRepository
import com.mbd.cmsdesktop.data.cache.DesktopBootstrapSnapshotStore
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton

/** Durable cache-first session-fee repository. */
@Singleton
class DesktopSessionFeeRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val store: DesktopBootstrapSnapshotStore,
    private val sessionManager: SessionManager,
) : SessionFeeRepository {
    override suspend fun getSessionFee(sessionId: String): SessionFeeStructure? {
        val fee = cachedFees().firstOrNull { it.sessionId == sessionId && !it.isDeleted } ?: return null
        val heads = cachedHeads().filter { it.sessionId == sessionId && !it.isDeleted }.sortedBy { it.position }
        return DesktopSessionFeeMapper.dtoToDomain(fee, heads)
    }

    override suspend fun syncSession(sessionId: String) {
        val scope = SyncCheckpointDefaults.scoped("session" to sessionId)
        val feesDelta = fetchIncrementalDelta(
            store, ownerKey(), SupabaseTables.SESSION_FEES, scope, SessionFeeDto::updatedAt,
        ) { since, from, to ->
            postgrest.from(SupabaseTables.SESSION_FEES).select {
                filter {
                    eq("session_id", sessionId)
                    gte("updated_at", since)
                }
                order("updated_at", Order.ASCENDING)
                range(from, to)
            }.decodeList()
        }
        val headsDelta = fetchIncrementalDelta(
            store, ownerKey(), SupabaseTables.SESSION_FEE_HEADS, scope, SessionFeeHeadDto::updatedAt,
        ) { since, from, to ->
            postgrest.from(SupabaseTables.SESSION_FEE_HEADS).select {
                filter {
                    eq("session_id", sessionId)
                    gte("updated_at", since)
                }
                order("updated_at", Order.ASCENDING)
                range(from, to)
            }.decodeList()
        }
        store.writeRows(FEES_FILE, SessionFeeDto.serializer(), mergeIncrementalDelta(
            cachedFees(), feesDelta, { it.sessionId.orEmpty() }, SessionFeeDto::isDeleted,
        ))
        store.writeRows(HEADS_FILE, SessionFeeHeadDto.serializer(), mergeIncrementalDelta(
            cachedHeads(), headsDelta, { "${it.sessionId}|${it.label}" }, SessionFeeHeadDto::isDeleted,
        ))
    }

    override suspend fun saveSessionFee(structure: SessionFeeStructure, updatedBy: String) {
        require(structure.heads.all { it.label.trim().isNotBlank() }) { "Every fee head needs a label." }
        require(structure.heads.all { it.amount > 0.0 }) { "Every fee amount must be greater than zero." }

        val feeDto = DesktopSessionFeeMapper.domainToFeeDto(structure, updatedBy)
        postgrest.from(SupabaseTables.SESSION_FEES).upsert(feeDto) { onConflict = "session_id" }

        postgrest.from(SupabaseTables.SESSION_FEE_HEADS).update({
            set("is_deleted", true)
            set("updated_by", updatedBy)
        }) { filter { eq("session_id", structure.sessionId) } }

        val heads = DesktopSessionFeeMapper.domainToHeadDtos(structure, updatedBy)
            .map { it.copy(isDeleted = false, deletedAt = null, deletedBy = null) }
        if (heads.isNotEmpty()) {
            postgrest.from(SupabaseTables.SESSION_FEE_HEADS).upsert(heads) {
                onConflict = "session_id,label"
            }
        }

        store.writeRows(FEES_FILE, SessionFeeDto.serializer(), mergeIncrementalDelta(
            cachedFees(), listOf(feeDto), { it.sessionId.orEmpty() }, SessionFeeDto::isDeleted,
        ))
        store.writeRows(
            HEADS_FILE,
            SessionFeeHeadDto.serializer(),
            cachedHeads().filterNot { it.sessionId == structure.sessionId } + heads,
        )
    }

    private fun cachedFees() = store.readRows(FEES_FILE, SessionFeeDto.serializer())
    private fun cachedHeads() = store.readRows(HEADS_FILE, SessionFeeHeadDto.serializer())

    private fun ownerKey() =
        sessionManager.accountKey ?: SyncCheckpointDefaults.ownerKey("anonymous-local")

    private companion object {
        const val FEES_FILE = "session-fees.json"
        const val HEADS_FILE = "session-fee-heads.json"
    }
}
