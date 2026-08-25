package com.mbd.cmscommon.data.mapper

import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.dto.FineDto
import com.mbd.cmscommon.domain.model.Fine

/**
 * Direct DTO<->Domain mapping for the desktop apps (no local Room cache, so there is no
 * Entity intermediate here — just the same field logic mobile's FineRepositoryImpl.toDomain()
 * does).
 */
object DesktopFineMapper {
    fun dtoToDomain(dto: FineDto): Fine = Fine(
        id = dto.id ?: "",
        sessionId = dto.sessionId ?: "",
        rollNumber = dto.rollNumber ?: "",
        category = dto.category ?: "",
        amount = dto.amount,
        reason = dto.reason ?: "",
        issuedBy = dto.issuedBy,
        issuedAt = PgTime.parse(dto.issuedAt),
        entityId = dto.entityId ?: 0L,
        createdAt = PgTime.parseOrEpoch(dto.createdAt),
        createdBy = dto.createdBy,
        updatedAt = PgTime.parseOrEpoch(dto.updatedAt),
        updatedBy = dto.updatedBy,
    )

    fun domainToDto(domain: Fine): FineDto = FineDto(
        id = domain.id.ifBlank { null },
        sessionId = domain.sessionId,
        rollNumber = domain.rollNumber,
        category = domain.category.ifBlank { "OTHER" },
        amount = domain.amount,
        reason = domain.reason.trim(),
        issuedBy = domain.issuedBy,
        createdBy = domain.createdBy,
        updatedBy = domain.updatedBy,
    )
}
