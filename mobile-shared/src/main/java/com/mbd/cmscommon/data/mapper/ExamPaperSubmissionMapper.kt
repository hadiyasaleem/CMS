package com.mbd.cmscommon.data.mapper

import com.mbd.cmscommon.data.local.entity.ExamPaperSubmissionEntity
import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.dto.ExamPaperSubmissionDto
import com.mbd.cmscommon.domain.model.ExamPaperSubmission
import com.mbd.cmscommon.domain.model.ExamType
import java.time.Instant

object ExamPaperSubmissionMapper {
    private fun parseExamType(raw: String?): ExamType =
        runCatching { ExamType.valueOf(raw ?: "") }.getOrDefault(ExamType.MIDTERM)

    fun dtoToDomain(dto: ExamPaperSubmissionDto): ExamPaperSubmission = ExamPaperSubmission(
        submissionId = dto.id ?: "",
        offeringId = dto.sessionId ?: "",
        subjectId = dto.courseCode ?: "",
        examType = parseExamType(dto.examType),
        teacherId = dto.teacherEmail ?: "",
        storagePath = dto.storagePath ?: "",
        fileName = dto.fileName ?: "",
        uploadedAt = PgTime.parseOrEpoch(dto.uploadedAt),
        entityId = dto.entityId ?: 0L,
        createdAt = PgTime.parseOrEpoch(dto.createdAt),
        createdBy = dto.createdBy ?: "",
        updatedAt = PgTime.parseOrEpoch(dto.updatedAt),
        updatedBy = dto.updatedBy,
    )

    fun domainToEntity(domain: ExamPaperSubmission): ExamPaperSubmissionEntity = ExamPaperSubmissionEntity(
        submissionId = domain.submissionId,
        offeringId = domain.offeringId,
        subjectId = domain.subjectId,
        examType = domain.examType.name,
        teacherId = domain.teacherId,
        storagePath = domain.storagePath,
        fileName = domain.fileName,
        uploadedAt = domain.uploadedAt.toEpochMilli(),
        createdBy = domain.createdBy,
        entityId = domain.entityId,
        createdAt = domain.createdAt.toEpochMilli(),
        updatedAt = domain.updatedAt.toEpochMilli(),
        updatedBy = domain.updatedBy,
    )

    fun entityToDomain(entity: ExamPaperSubmissionEntity): ExamPaperSubmission = ExamPaperSubmission(
        submissionId = entity.submissionId,
        offeringId = entity.offeringId,
        subjectId = entity.subjectId,
        examType = parseExamType(entity.examType),
        teacherId = entity.teacherId,
        storagePath = entity.storagePath ?: "",
        fileName = entity.fileName ?: "",
        uploadedAt = Instant.ofEpochMilli(entity.uploadedAt),
        entityId = entity.entityId,
        createdAt = Instant.ofEpochMilli(entity.createdAt),
        createdBy = entity.createdBy ?: "",
        updatedAt = Instant.ofEpochMilli(entity.updatedAt),
        updatedBy = entity.updatedBy,
    )

    fun dtoToEntity(dto: ExamPaperSubmissionDto): ExamPaperSubmissionEntity = domainToEntity(dtoToDomain(dto))
}
