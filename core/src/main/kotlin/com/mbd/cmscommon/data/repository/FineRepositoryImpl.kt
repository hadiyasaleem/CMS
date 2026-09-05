package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.FineDto
import com.mbd.cmscommon.domain.model.Fine
import com.mbd.cmscommon.domain.repository.FineRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject

class FineRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
) : FineRepository {

    override suspend fun getFines(sessionId: String, rollNumber: String): List<Fine> {
        val rows = postgrest.from(SupabaseTables.FINES).select {
            filter {
                eq("session_id", sessionId)
                eq("roll_number", rollNumber)
            }
            order("issued_at", Order.DESCENDING)
        }.decodeList<FineDto>()
        return rows.map { it.toDomain() }
    }

    override suspend fun issueFine(
        sessionId: String,
        rollNumber: String,
        category: String,
        amount: Double,
        reason: String,
        issuedBy: String,
    ) {
        val dto = FineDto(
            sessionId = sessionId,
            rollNumber = rollNumber,
            category = category.ifBlank { "OTHER" },
            amount = amount,
            reason = reason.trim(),
            issuedBy = issuedBy,
        )
        postgrest.from(SupabaseTables.FINES).insert(dto)
    }

    override suspend fun deleteFine(id: String) {
        postgrest.from(SupabaseTables.FINES).delete {
            filter { eq("id", id) }
        }
    }

    private fun FineDto.toDomain() = Fine(
        id = id.orEmpty(),
        sessionId = sessionId.orEmpty(),
        rollNumber = rollNumber.orEmpty(),
        category = category.orEmpty(),
        amount = amount,
        reason = reason.orEmpty(),
        issuedBy = issuedBy,
        issuedAt = PgTime.parse(issuedAt),
        createdAt = PgTime.parseOrEpoch(createdAt),
        createdBy = createdBy,
        updatedAt = PgTime.parseOrEpoch(updatedAt),
        updatedBy = updatedBy,
    )
}
