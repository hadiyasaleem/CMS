package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.data.mapper.DesktopFineMapper
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.FineDto
import com.mbd.cmscommon.domain.model.Fine
import com.mbd.cmscommon.domain.repository.FineRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Desktop repos are always-online: no local persistence, every call re-fetches from Postgrest.
 *
 * [FineRepository] exposes no `observe*`/`sync()` methods, so there is nothing for a screen to
 * subscribe to — [cache] is kept purely as a small in-memory memoization keyed by
 * "sessionId|rollNumber" (mirroring the template's cache-then-refresh shape) rather than being
 * exposed as a Flow; [getFines] always re-fetches and refreshes it.
 */
@Singleton
class DesktopFineRepository @Inject constructor(
    private val postgrest: Postgrest,
) : FineRepository {

    private val cache = MutableStateFlow<Map<String, List<Fine>>>(emptyMap())

    private fun cacheKey(sessionId: String, rollNumber: String) = "$sessionId|$rollNumber"

    override suspend fun getFines(sessionId: String, rollNumber: String): List<Fine> {
        val rows = postgrest.from(SupabaseTables.FINES).select {
            filter {
                eq("session_id", sessionId)
                eq("roll_number", rollNumber)
                eq("is_deleted", false)
            }
            order("issued_at", Order.DESCENDING)
        }.decodeList<FineDto>()
        val fines = rows.map { DesktopFineMapper.dtoToDomain(it) }
        cache.update { it + (cacheKey(sessionId, rollNumber) to fines) }
        return fines
    }

    override suspend fun issueFine(sessionId: String, rollNumber: String, category: String, amount: Double, reason: String, issuedBy: String) {
        val dto = FineDto(
            sessionId = sessionId,
            rollNumber = rollNumber,
            category = category.ifBlank { "OTHER" },
            amount = amount,
            reason = reason.trim(),
            issuedBy = issuedBy,
        )
        postgrest.from(SupabaseTables.FINES).insert(dto)
        getFines(sessionId, rollNumber)
    }

    override suspend fun deleteFine(id: String) {
        postgrest.from(SupabaseTables.FINES).update({ set("is_deleted", true) }) {
            filter { eq("id", id) }
        }
        cache.update { m -> m.mapValues { (_, fines) -> fines.filterNot { it.id == id } } }
    }
}
