package com.mbd.cmscommon.data.mapper

import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.dto.MarkEditRequestDto
import com.mbd.cmscommon.domain.model.ExamType
import com.mbd.cmscommon.domain.model.MarkEditRequest
import com.mbd.cmscommon.domain.model.MarkEditStatus

/**
 * Direct DTO<->Domain mapping for the desktop apps (no local Room cache, so there is no Entity
 * intermediate here — just the same field logic mobile's MarkEditRequestRepositoryImpl.toDomain()
 * does).
 */
object DesktopMarkEditRequestMapper {
    const val STATUS_PENDING = "PENDING"
    const val STATUS_APPROVED = "APPROVED"
    const val STATUS_REJECTED = "REJECTED"

    fun dtoToDomain(dto: MarkEditRequestDto): MarkEditRequest = MarkEditRequest(
        id = dto.id ?: "",
        sessionId = dto.sessionId ?: "",
        semester = dto.semester,
        courseCode = dto.courseCode ?: "",
        examType = runCatching { ExamType.valueOf(dto.examType ?: "") }.getOrDefault(ExamType.MIDTERM),
        rollNumber = dto.rollNumber ?: "",
        currentScore = dto.currentScore,
        requestedScore = dto.requestedScore,
        reason = dto.reason,
        status = runCatching { MarkEditStatus.valueOf(dto.status ?: "") }.getOrDefault(MarkEditStatus.PENDING),
        requestedBy = dto.requestedBy,
        reviewedBy = dto.reviewedBy,
        requestedAt = PgTime.parseOrEpoch(dto.requestedAt),
        reviewedAt = PgTime.parse(dto.reviewedAt),
        entityId = dto.entityId ?: 0L,
        createdAt = PgTime.parseOrEpoch(dto.createdAt),
        createdBy = dto.createdBy,
        updatedAt = PgTime.parseOrEpoch(dto.updatedAt),
        updatedBy = dto.updatedBy,
    )

    fun domainToDto(domain: MarkEditRequest): MarkEditRequestDto = MarkEditRequestDto(
        id = domain.id.ifBlank { null },
        sessionId = domain.sessionId,
        semester = domain.semester,
        courseCode = domain.courseCode,
        examType = domain.examType.name,
        rollNumber = domain.rollNumber,
        currentScore = domain.currentScore,
        requestedScore = domain.requestedScore,
        reason = domain.reason?.trim()?.takeIf { it.isNotBlank() },
        status = domain.status.name,
        requestedBy = domain.requestedBy,
        reviewedBy = domain.reviewedBy,
        createdBy = domain.createdBy,
        updatedBy = domain.updatedBy,
    )
}
