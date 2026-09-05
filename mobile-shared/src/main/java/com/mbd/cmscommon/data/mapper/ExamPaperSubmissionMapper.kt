package com.mbd.cmscommon.data.mapper

import com.mbd.cmscommon.data.local.entity.ExamPaperSubmissionEntity
import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.dto.ExamPaperSubmissionDto
import com.mbd.cmscommon.domain.model.ExamPaperReviewStatus
import com.mbd.cmscommon.domain.model.ExamPaperSubmission
import com.mbd.cmscommon.domain.model.ExamType
import java.time.Instant

object ExamPaperSubmissionMapper {
    private fun parseExamType(raw: String?): ExamType =
        runCatching { ExamType.valueOf(raw?.trim()?.uppercase() ?: "") }.getOrDefault(ExamType.MIDTERM)

    private fun parseReviewStatus(raw: String?): ExamPaperReviewStatus =
        runCatching { ExamPaperReviewStatus.valueOf(raw?.trim()?.uppercase() ?: "") }.getOrDefault(ExamPaperReviewStatus.SUBMITTED)

    fun dtoToDomain(dto: ExamPaperSubmissionDto): ExamPaperSubmission = ExamPaperSubmission(
        submissionId = dto.id ?: "",
        offeringId = dto.sessionId ?: "",
        subjectId = dto.courseCode ?: "",
        examType = parseExamType(dto.examType),
        teacherId = dto.teacherEmail ?: "",
        storagePath = dto.storagePath ?: "",
        fileName = dto.fileName ?: "",
        uploadedAt = PgTime.parseOrEpoch(dto.uploadedAt),
        mimeType = dto.mimeType,
        reviewStatus = parseReviewStatus(dto.reviewStatus),
        reviewedBy = dto.reviewedBy,
        reviewedAt = PgTime.parse(dto.reviewedAt),
        teacherNotes = dto.teacherNotes,
        keyStoragePath = dto.keyStoragePath,
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
        mimeType = domain.mimeType,
        keyStoragePath = domain.keyStoragePath,
        teacherNotes = domain.teacherNotes,
        reviewStatus = domain.reviewStatus.name,
        reviewedBy = domain.reviewedBy,
        reviewedAt = domain.reviewedAt?.toEpochMilli(),
        createdBy = domain.createdBy,
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
        mimeType = entity.mimeType,
        reviewStatus = parseReviewStatus(entity.reviewStatus),
        reviewedBy = entity.reviewedBy,
        reviewedAt = entity.reviewedAt?.let { Instant.ofEpochMilli(it) },
        teacherNotes = entity.teacherNotes,
        keyStoragePath = entity.keyStoragePath,
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
