package com.mbd.cmscommon.data.mapper

import com.mbd.cmscommon.data.local.entity.ExamPaperSubmissionEntity
import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.dto.ExamPaperSubmissionDto
import com.mbd.cmscommon.domain.model.ExamPaperSubmission
import java.time.Instant

object ExamPaperSubmissionMapper {
    fun dtoToDomain(dto: ExamPaperSubmissionDto): ExamPaperSubmission = ExamPaperSubmission(
        submissionId = dto.id ?: "",
        datesheetSlotId = dto.datesheetSlotId ?: "",
        offeringId = dto.sessionId ?: "",
        semester = dto.semester,
        subjectId = dto.courseCode ?: "",
        teacherId = dto.teacherEmail ?: "",
        storagePath = dto.storagePath ?: "",
        fileName = dto.fileName ?: "",
        fileSizeBytes = dto.fileSizeBytes,
        uploadedAt = PgTime.parseOrEpoch(dto.uploadedAt),
        mimeType = dto.mimeType,
        description = dto.description,
        createdAt = PgTime.parseOrEpoch(dto.createdAt),
        createdBy = dto.createdBy ?: "",
        updatedAt = PgTime.parseOrEpoch(dto.updatedAt),
        updatedBy = dto.updatedBy,
    )

    fun domainToEntity(domain: ExamPaperSubmission): ExamPaperSubmissionEntity = ExamPaperSubmissionEntity(
        submissionId = domain.submissionId,
        datesheetSlotId = domain.datesheetSlotId,
        offeringId = domain.offeringId,
        semester = domain.semester,
        subjectId = domain.subjectId,
        teacherId = domain.teacherId,
        storagePath = domain.storagePath,
        fileName = domain.fileName,
        fileSizeBytes = domain.fileSizeBytes,
        uploadedAt = domain.uploadedAt.toEpochMilli(),
        mimeType = domain.mimeType,
        description = domain.description,
        createdBy = domain.createdBy,
        createdAt = domain.createdAt.toEpochMilli(),
        updatedAt = domain.updatedAt.toEpochMilli(),
        updatedBy = domain.updatedBy,
    )

    fun entityToDomain(entity: ExamPaperSubmissionEntity): ExamPaperSubmission = ExamPaperSubmission(
        submissionId = entity.submissionId,
        datesheetSlotId = entity.datesheetSlotId,
        offeringId = entity.offeringId,
        semester = entity.semester,
        subjectId = entity.subjectId,
        teacherId = entity.teacherId,
        storagePath = entity.storagePath ?: "",
        fileName = entity.fileName ?: "",
        fileSizeBytes = entity.fileSizeBytes,
        uploadedAt = Instant.ofEpochMilli(entity.uploadedAt),
        mimeType = entity.mimeType,
        description = entity.description,
        createdAt = Instant.ofEpochMilli(entity.createdAt),
        createdBy = entity.createdBy ?: "",
        updatedAt = Instant.ofEpochMilli(entity.updatedAt),
        updatedBy = entity.updatedBy,
    )

    fun dtoToEntity(dto: ExamPaperSubmissionDto): ExamPaperSubmissionEntity = domainToEntity(dtoToDomain(dto)).copy(
        isDeleted = dto.isDeleted,
        deletedAt = PgTime.parse(dto.deletedAt)?.toEpochMilli(),
        deletedBy = dto.deletedBy,
    )
}
