package com.mbd.cmscommon.data.mapper

import com.mbd.cmscommon.data.local.entity.MarkEditRequestEntity
import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.dto.MarkEditRequestDto
import com.mbd.cmscommon.domain.model.ExamType
import com.mbd.cmscommon.domain.model.MarkEditRequest
import com.mbd.cmscommon.domain.model.MarkEditStatus
import java.time.Instant

object MarkEditRequestEntityMapper {
    fun dtoToEntity(dto: MarkEditRequestDto): MarkEditRequestEntity = MarkEditRequestEntity(
        requestId = dto.id ?: "",
        sessionId = dto.sessionId ?: "",
        semester = dto.semester,
        courseCode = dto.courseCode ?: "",
        examType = dto.examType ?: "",
        rollNumber = dto.rollNumber ?: "",
        currentScore = dto.currentScore,
        requestedScore = dto.requestedScore,
        reason = dto.reason,
        status = dto.status ?: "PENDING",
        requestedBy = dto.requestedBy ?: "",
        reviewedBy = dto.reviewedBy,
        requestedAt = PgTime.parseOrEpoch(dto.requestedAt).toEpochMilli(),
        reviewedAt = PgTime.parse(dto.reviewedAt)?.toEpochMilli(),
        createdAt = PgTime.parseOrEpoch(dto.createdAt).toEpochMilli(),
        createdBy = dto.createdBy,
        updatedAt = PgTime.parseOrEpoch(dto.updatedAt).toEpochMilli(),
        updatedBy = dto.updatedBy,
        isDeleted = dto.isDeleted,
        deletedAt = PgTime.parse(dto.deletedAt)?.toEpochMilli(),
        deletedBy = dto.deletedBy,
    )

    fun entityToDomain(entity: MarkEditRequestEntity): MarkEditRequest = MarkEditRequest(
        id = entity.requestId,
        sessionId = entity.sessionId,
        semester = entity.semester,
        courseCode = entity.courseCode,
        examType = runCatching { ExamType.valueOf(entity.examType.trim().uppercase()) }.getOrDefault(ExamType.MIDTERM),
        rollNumber = entity.rollNumber,
        currentScore = entity.currentScore,
        requestedScore = entity.requestedScore,
        reason = entity.reason,
        status = runCatching { MarkEditStatus.valueOf(entity.status.trim().uppercase()) }.getOrDefault(MarkEditStatus.PENDING),
        requestedBy = entity.requestedBy,
        reviewedBy = entity.reviewedBy,
        requestedAt = Instant.ofEpochMilli(entity.requestedAt),
        reviewedAt = entity.reviewedAt?.let { Instant.ofEpochMilli(it) },
        createdAt = Instant.ofEpochMilli(entity.createdAt),
        createdBy = entity.createdBy,
        updatedAt = Instant.ofEpochMilli(entity.updatedAt),
        updatedBy = entity.updatedBy,
    )
}
