package com.mbd.cmscommon.data.mapper

import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.dto.StudentLinkRequestDto
import com.mbd.cmscommon.domain.model.LinkRequestStatus
import com.mbd.cmscommon.domain.model.StudentLinkRequest

/**
 * Direct DTO<->Domain mapping for the desktop apps (no local Room cache, so there is no Entity
 * intermediate here — just the same field logic mobile's StudentLinkRequestMapper.dtoToDomain +
 * domainToEntity pair does, composed into one step).
 */
object DesktopStudentLinkRequestMapper {
    private fun String?.emptyToNull(): String? = if (this == null || isBlank()) null else this

    private fun parseStatus(raw: String?): LinkRequestStatus =
        runCatching { LinkRequestStatus.valueOf(raw ?: "") }.getOrDefault(LinkRequestStatus.PENDING)

    fun dtoToDomain(dto: StudentLinkRequestDto): StudentLinkRequest = StudentLinkRequest(
        requestId = dto.requestId ?: "",
        requestedByUid = dto.requestedByEmail ?: "",
        sessionIdClaimed = dto.sessionId.emptyToNull(),
        rollNumberClaimed = dto.rollNumberClaimed ?: "",
        nameClaimed = dto.nameClaimed.emptyToNull(),
        cnicClaimed = dto.cnicClaimed.emptyToNull(),
        dobClaimed = dto.dobClaimed.emptyToNull(),
        universityRollClaimed = dto.universityRollClaimed.emptyToNull(),
        registrationNoClaimed = dto.registrationNoClaimed.emptyToNull(),
        message = dto.message.emptyToNull(),
        status = parseStatus(dto.status),
        reviewedBy = dto.reviewedBy.emptyToNull(),
        reviewedAt = PgTime.parse(dto.reviewedAt),
        rejectionReason = dto.rejectionReason.emptyToNull(),
        attemptCount = dto.attemptCount,
        entityId = dto.entityId ?: 0L,
        createdAt = PgTime.parseOrEpoch(dto.createdAt),
        createdBy = dto.createdBy,
        updatedAt = PgTime.parseOrEpoch(dto.updatedAt),
        updatedBy = dto.updatedBy,
    )

    fun domainToDto(domain: StudentLinkRequest): StudentLinkRequestDto = StudentLinkRequestDto(
        requestId = domain.requestId.ifBlank { null },
        requestedByEmail = domain.requestedByUid,
        rollNumberClaimed = domain.rollNumberClaimed,
        sessionId = domain.sessionIdClaimed,
        nameClaimed = domain.nameClaimed,
        cnicClaimed = domain.cnicClaimed,
        dobClaimed = domain.dobClaimed,
        universityRollClaimed = domain.universityRollClaimed,
        registrationNoClaimed = domain.registrationNoClaimed,
        message = domain.message,
        status = domain.status.name,
        reviewedBy = domain.reviewedBy,
        rejectionReason = domain.rejectionReason,
        attemptCount = domain.attemptCount,
        createdBy = domain.createdBy,
        updatedBy = domain.updatedBy,
    )
}
