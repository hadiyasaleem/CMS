package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.data.mapper.DesktopSessionFeeMapper
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.SessionFeeDto
import com.mbd.cmscommon.data.remote.dto.SessionFeeHeadDto
import com.mbd.cmscommon.domain.model.SessionFeeStructure
import com.mbd.cmscommon.domain.repository.SessionFeeRepository
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Desktop repos are always-online: no local persistence, every call re-fetches from Postgrest.
 *
 * Unlike the other session repositories in this file group, [SessionFeeRepository] exposes no
 * `observe*`/`sync()` methods — every consumer just calls the suspend [getSessionFee] directly —
 * so there is nothing for a screen to subscribe to. [cache] is kept purely as a small in-memory
 * memoization keyed by sessionId (mirroring the template's cache-then-refresh shape) rather than
 * being exposed as a Flow; [getSessionFee] always re-fetches and refreshes it, it never serves a
 * stale read from the cache.
 */
@Singleton
class DesktopSessionFeeRepository @Inject constructor(
    private val postgrest: Postgrest,
) : SessionFeeRepository {

    private val cache = MutableStateFlow<Map<String, SessionFeeStructure>>(emptyMap())

    override suspend fun getSessionFee(sessionId: String): SessionFeeStructure? {
        val feeDto = postgrest.from(SupabaseTables.SESSION_FEES).select {
            filter { eq("session_id", sessionId) }
        }.decodeList<SessionFeeDto>().filterNot { it.isDeleted }.firstOrNull() ?: run {
            cache.update { it - sessionId }
            return null
        }
        val heads = postgrest.from(SupabaseTables.SESSION_FEE_HEADS).select {
            filter { eq("session_id", sessionId) }
        }.decodeList<SessionFeeHeadDto>().filterNot { it.isDeleted }

        val domain = DesktopSessionFeeMapper.dtoToDomain(feeDto, heads)
        cache.update { it + (sessionId to domain) }
        return domain
    }

    override suspend fun saveSessionFee(structure: SessionFeeStructure, updatedBy: String) {
        val feeDto = DesktopSessionFeeMapper.domainToFeeDto(structure, updatedBy)
        postgrest.from(SupabaseTables.SESSION_FEES).upsert(feeDto) { onConflict = "session_id" }

        val headDtos = DesktopSessionFeeMapper.domainToHeadDtos(structure, updatedBy)
        postgrest.from(SupabaseTables.SESSION_FEE_HEADS).delete { filter { eq("session_id", structure.sessionId) } }
        if (headDtos.isNotEmpty()) {
            postgrest.from(SupabaseTables.SESSION_FEE_HEADS).insert(headDtos)
        }

        getSessionFee(structure.sessionId)
    }
}
