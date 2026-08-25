package com.mbd.cmsdesktop.data.repository

import com.mbd.cmscommon.data.mapper.DesktopSessionFeeMapper
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.SessionFeeDto
import com.mbd.cmscommon.data.remote.dto.SessionFeeHeadDto
import com.mbd.cmscommon.domain.model.SessionFeeStructure
import com.mbd.cmscommon.domain.repository.SessionFeeRepository
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pure Postgrest passthrough — no in-memory cache at all. [SessionFeeRepository] exposes no
 * `observe*`/`sync()`, so every consumer just calls [getSessionFee] directly and always gets a
 * fresh read.
 */
@Singleton
class DesktopSessionFeeRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
) : SessionFeeRepository {

    override suspend fun getSessionFee(sessionId: String): SessionFeeStructure? {
        val feeDto = postgrest.from(SupabaseTables.SESSION_FEES).select {
            filter { eq("session_id", sessionId) }
        }.decodeList<SessionFeeDto>().filterNot { it.isDeleted }.firstOrNull() ?: return null

        val heads = postgrest.from(SupabaseTables.SESSION_FEE_HEADS).select {
            filter { eq("session_id", sessionId) }
        }.decodeList<SessionFeeHeadDto>().filterNot { it.isDeleted }

        return DesktopSessionFeeMapper.dtoToDomain(feeDto, heads)
    }

    override suspend fun saveSessionFee(structure: SessionFeeStructure, updatedBy: String) {
        val feeDto = DesktopSessionFeeMapper.domainToFeeDto(structure, updatedBy)
        postgrest.from(SupabaseTables.SESSION_FEES).upsert(feeDto) { onConflict = "session_id" }

        val headDtos = DesktopSessionFeeMapper.domainToHeadDtos(structure, updatedBy)
        postgrest.from(SupabaseTables.SESSION_FEE_HEADS).delete { filter { eq("session_id", structure.sessionId) } }
        if (headDtos.isNotEmpty()) {
            postgrest.from(SupabaseTables.SESSION_FEE_HEADS).insert(headDtos)
        }
    }
}
