package com.mbd.cmscommon.data.mapper

import com.mbd.cmscommon.data.local.entity.StudentLinkRequestEntity
import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.dto.StudentLinkRequestDto
import com.mbd.cmscommon.domain.model.LinkRequestStatus
import com.mbd.cmscommon.domain.model.StudentLinkRequest
import java.time.Instant

object StudentLinkRequestMapper {
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

    fun domainToEntity(domain: StudentLinkRequest): StudentLinkRequestEntity = StudentLinkRequestEntity(
        requestId = domain.requestId,
        requestedByUid = domain.requestedByUid,
        sessionIdClaimed = domain.sessionIdClaimed,
        rollNumberClaimed = domain.rollNumberClaimed,
        nameClaimed = domain.nameClaimed,
        cnicClaimed = domain.cnicClaimed,
        dobClaimed = domain.dobClaimed,
        universityRollClaimed = domain.universityRollClaimed,
        registrationNoClaimed = domain.registrationNoClaimed,
        message = domain.message,
        status = domain.status.name,
        reviewedBy = domain.reviewedBy,
        reviewedAt = domain.reviewedAt?.toEpochMilli(),
        rejectionReason = domain.rejectionReason,
        attemptCount = domain.attemptCount,
        createdAt = domain.createdAt.toEpochMilli(),
        entityId = domain.entityId,
        createdBy = domain.createdBy,
        updatedAt = domain.updatedAt.toEpochMilli(),
        updatedBy = domain.updatedBy,
    )

    fun entityToDomain(entity: StudentLinkRequestEntity): StudentLinkRequest = StudentLinkRequest(
        requestId = entity.requestId,
        requestedByUid = entity.requestedByUid,
        sessionIdClaimed = entity.sessionIdClaimed,
        rollNumberClaimed = entity.rollNumberClaimed ?: "",
        nameClaimed = entity.nameClaimed,
        cnicClaimed = entity.cnicClaimed,
        dobClaimed = entity.dobClaimed,
        universityRollClaimed = entity.universityRollClaimed,
        registrationNoClaimed = entity.registrationNoClaimed,
        message = entity.message,
        status = parseStatus(entity.status),
        reviewedBy = entity.reviewedBy,
        reviewedAt = entity.reviewedAt?.let { Instant.ofEpochMilli(it) },
        rejectionReason = entity.rejectionReason,
        attemptCount = entity.attemptCount,
        entityId = entity.entityId,
        createdAt = Instant.ofEpochMilli(entity.createdAt),
        createdBy = entity.createdBy,
        updatedAt = Instant.ofEpochMilli(entity.updatedAt),
        updatedBy = entity.updatedBy,
    )

    fun dtoToEntity(dto: StudentLinkRequestDto): StudentLinkRequestEntity = domainToEntity(dtoToDomain(dto)).copy(
        isDeleted = dto.isDeleted,
        deletedAt = PgTime.parse(dto.deletedAt)?.toEpochMilli(),
        deletedBy = dto.deletedBy,
    )
}
