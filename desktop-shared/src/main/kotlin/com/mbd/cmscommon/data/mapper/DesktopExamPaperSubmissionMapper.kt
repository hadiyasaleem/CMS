package com.mbd.cmscommon.data.mapper

import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.dto.ExamPaperSubmissionDto
import com.mbd.cmscommon.domain.model.ExamPaperSubmission
import com.mbd.cmscommon.domain.model.ExamType

/**
 * Direct DTO<->Domain mapping for the desktop apps (no local Room cache, so there is no
 * Entity intermediate here — just the same field logic mobile's dtoToEntity+entityToDomain
 * pair does, composed into one step).
 */
object DesktopExamPaperSubmissionMapper {
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
}
