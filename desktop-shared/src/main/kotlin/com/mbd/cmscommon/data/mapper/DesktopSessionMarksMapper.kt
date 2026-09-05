package com.mbd.cmscommon.data.mapper

import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.dto.MarkRowDto
import com.mbd.cmscommon.data.remote.dto.SemesterGpaDto
import com.mbd.cmscommon.domain.model.ExamType
import com.mbd.cmscommon.domain.model.SemesterGpa
import com.mbd.cmscommon.domain.model.SubjectExamScore
import java.time.LocalDate

/**
 * Direct DTO<->Domain mapping for session marks + semester GPA, for the desktop apps (no local
 * Room cache — just the same field logic mobile's toEntity/toDomain pair does, composed into one
 * step). There is no single domain "mark row" model (the domain surface is a set of derived
 * views — score maps, absent-roll sets, per-student score lists), so [dtoToDomain] targets the
 * per-student-per-exam [SubjectExamScore] the repository layer composes those views from, and
 * [domainToDto] takes the raw pieces [SessionMarksRepositoryImpl.saveScores] needs rather than a
 * single domain object.
 */
object DesktopSessionMarksMapper {
    fun dtoToDomain(dto: MarkRowDto): SubjectExamScore? {
        val type = dto.examType?.let { runCatching { ExamType.valueOf(it) }.getOrNull() } ?: return null
        return SubjectExamScore(
            courseCode = dto.courseCode ?: "",
            examType = type,
            score = dto.score ?: 0,
            maxMarks = dto.maxMarks,
            wasAbsent = dto.wasAbsent,
            remarks = dto.remarks,
        )
    }

    fun domainToDto(
        sessionId: String,
        semester: Int,
        courseCode: String,
        examType: ExamType,
        rollNumber: String,
        score: Int,
        wasAbsent: Boolean,
        examDate: LocalDate?,
        teacherEmail: String,
    ): MarkRowDto = MarkRowDto(
        sessionId = sessionId,
        semester = semester,
        courseCode = courseCode,
        examType = examType.name,
        rollNumber = rollNumber,
        score = score,
        maxMarks = examType.maxMarks,
        wasAbsent = wasAbsent,
        examDate = examDate?.toString(),
        teacherEmail = teacherEmail,
    )

    fun gpaDtoToDomain(dto: SemesterGpaDto): SemesterGpa = SemesterGpa(
        sessionId = dto.sessionId ?: "",
        rollNumber = dto.rollNumber ?: "",
        semester = dto.semester,
        gpa = dto.gpa,
        cgpa = dto.cgpa,
        termLabel = dto.termLabel,
        resultStatus = dto.resultStatus ?: "PENDING",
        classPosition = dto.classPosition,
        remarks = dto.remarks,
        supplyCourses = dto.supplyCourses,
        createdAt = PgTime.parseOrEpoch(dto.createdAt),
        createdBy = dto.createdBy,
        updatedAt = PgTime.parseOrEpoch(dto.updatedAt),
        updatedBy = dto.updatedBy,
    )
}
