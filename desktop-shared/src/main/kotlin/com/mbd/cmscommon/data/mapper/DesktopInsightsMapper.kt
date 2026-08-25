package com.mbd.cmscommon.data.mapper

import com.mbd.cmscommon.data.remote.dto.AtRiskStudentDto
import com.mbd.cmscommon.data.remote.dto.ExamStatDto
import com.mbd.cmscommon.data.remote.dto.SessionOverviewDto
import com.mbd.cmscommon.domain.model.AtRiskStudent
import com.mbd.cmscommon.domain.model.ExamStat
import com.mbd.cmscommon.domain.model.ExamType
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionOverview

/**
 * Direct DTO<->Domain mapping for the desktop apps. These three views are read-only (no inserts
 * or updates go through them), so there is no `domainToDto` counterpart — mirroring mobile's
 * InsightsRepositoryImpl, which maps these three view rows inline.
 */
object DesktopInsightsMapper {
    fun sessionOverviewToDomain(dto: SessionOverviewDto): SessionOverview = SessionOverview(
        sessionId = dto.sessionId ?: "",
        deptId = dto.deptId ?: "",
        shift = runCatching { Session.valueOf(dto.shift ?: "") }.getOrDefault(Session.MORNING),
        currentSemester = dto.currentSemester,
        students = dto.students.toInt(),
        avgCgpa = dto.avgCgpa,
        avgAttendance = dto.avgAttendance,
    )

    fun atRiskStudentToDomain(dto: AtRiskStudentDto): AtRiskStudent = AtRiskStudent(
        sessionId = dto.sessionId ?: "",
        rollNumber = dto.rollNumber ?: "",
        name = dto.name ?: "",
        cgpa = dto.cgpa,
        attendance = dto.attendance,
    )

    fun examStatToDomain(dto: ExamStatDto): ExamStat = ExamStat(
        sessionId = dto.sessionId ?: "",
        semester = dto.semester,
        courseCode = dto.courseCode ?: "",
        examType = runCatching { ExamType.valueOf(dto.examType ?: "") }.getOrDefault(ExamType.MIDTERM),
        entered = dto.entered.toInt(),
        avgScore = dto.avgScore,
        minScore = dto.minScore,
        maxScore = dto.maxScore,
        stddev = dto.stddev,
        outOf = dto.outOf,
        passRate = dto.passRate,
    )
}
