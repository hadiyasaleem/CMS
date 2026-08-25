package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.data.local.dao.SessionFeeDao
import com.mbd.cmscommon.data.mapper.SessionFeeMapper
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.SessionFeeDto
import com.mbd.cmscommon.data.remote.dto.SessionFeeHeadDto
import com.mbd.cmscommon.data.sync.SyncCheckpointDefaults
import com.mbd.cmscommon.data.sync.SyncCheckpointStore
import com.mbd.cmscommon.domain.model.SessionFeeStructure
import com.mbd.cmscommon.domain.repository.SessionFeeRepository
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Inject

class SessionFeeRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val feeDao: SessionFeeDao,
    private val checkpointStore: SyncCheckpointStore,
    private val sessionManager: SessionManager,
) : SessionFeeRepository {

    private fun syncOwnerKey(): String = sessionManager.accountKey ?: SyncCheckpointDefaults.ownerKey("anonymous-local")

    override suspend fun getSessionFee(sessionId: String): SessionFeeStructure? {
        runCatching { syncSessionFee(sessionId) }
        val fee = feeDao.getFee(sessionId) ?: return null
        val heads = feeDao.getHeads(sessionId)
        return SessionFeeMapper.toDomain(fee, heads)
    }

    override suspend fun saveSessionFee(structure: SessionFeeStructure, updatedBy: String) {
        val feeDto = SessionFeeDto(
            sessionId = structure.sessionId,
            cadence = structure.cadence.name,
            academicYear = structure.academicYear,
            dueDate = structure.dueDate,
            lateFineNote = structure.lateFineNote,
            paymentNote = structure.paymentNote,
            updatedBy = updatedBy,
        )
        postgrest.from(SupabaseTables.SESSION_FEES).upsert(feeDto) { onConflict = "session_id" }

        val headDtos = structure.heads.map { head ->
            SessionFeeHeadDto(
                sessionId = structure.sessionId,
                label = head.label,
                amount = head.amount,
                position = structure.heads.indexOf(head),
                updatedBy = updatedBy,
            )
        }
        postgrest.from(SupabaseTables.SESSION_FEE_HEADS).delete { filter { eq("session_id", structure.sessionId) } }
        if (headDtos.isNotEmpty()) {
            postgrest.from(SupabaseTables.SESSION_FEE_HEADS).insert(headDtos)
        }

        syncSessionFee(structure.sessionId)
    }

    private suspend fun syncSessionFee(sessionId: String) {
        syncFeeRow(sessionId)
        syncFeeHeads(sessionId)
    }

    private suspend fun syncFeeRow(sessionId: String) {
        val dto = postgrest.from(SupabaseTables.SESSION_FEES).select { filter { eq("session_id", sessionId) } }
            .decodeList<SessionFeeDto>().firstOrNull() ?: return
        feeDao.upsertFees(listOf(SessionFeeMapper.feeDtoToEntity(dto)))
    }

    private suspend fun syncFeeHeads(sessionId: String) {
        val rows = postgrest.from(SupabaseTables.SESSION_FEE_HEADS).select { filter { eq("session_id", sessionId) } }
            .decodeList<SessionFeeHeadDto>()
        feeDao.deleteHeadsForSession(sessionId)
        if (rows.isNotEmpty()) {
            feeDao.upsertHeads(rows.map { SessionFeeMapper.headDtoToEntity(it) })
        }
    }
}
