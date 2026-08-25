package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.AtRiskStudentDto
import com.mbd.cmscommon.data.remote.dto.ExamStatDto
import com.mbd.cmscommon.data.remote.dto.SessionOverviewDto
import com.mbd.cmscommon.domain.model.AtRiskStudent
import com.mbd.cmscommon.domain.model.ExamStat
import com.mbd.cmscommon.domain.model.ExamType
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionOverview
import com.mbd.cmscommon.domain.repository.InsightsRepository
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Inject

class InsightsRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
) : InsightsRepository {

    override suspend fun getSessionOverviews(): List<SessionOverview> {
        val rows = postgrest.from(SupabaseTables.VIEW_SESSION_OVERVIEW).select()
            .decodeList<SessionOverviewDto>()
        return rows.map {
            SessionOverview(
                sessionId = it.sessionId,
                deptId = it.deptId,
                shift = runCatching { Session.valueOf(it.shift) }.getOrDefault(Session.MORNING),
                currentSemester = it.currentSemester,
                students = it.students.toInt(),
                avgCgpa = it.avgCgpa,
                avgAttendance = it.avgAttendance,
            )
        }
    }

    override suspend fun getAtRiskStudents(): List<AtRiskStudent> {
        val rows = postgrest.from(SupabaseTables.VIEW_AT_RISK_STUDENTS).select()
            .decodeList<AtRiskStudentDto>()
        return rows.map {
            AtRiskStudent(
                sessionId = it.sessionId,
                rollNumber = it.rollNumber,
                name = it.name,
                cgpa = it.cgpa,
                attendance = it.attendance,
            )
        }
    }

    override suspend fun getExamStats(): List<ExamStat> {
        val rows = postgrest.from(SupabaseTables.VIEW_EXAM_STATS).select()
            .decodeList<ExamStatDto>()
        return rows.map {
            ExamStat(
                sessionId = it.sessionId,
                semester = it.semester,
                courseCode = it.courseCode,
                examType = runCatching { ExamType.valueOf(it.examType) }.getOrDefault(ExamType.MIDTERM),
                entered = it.entered.toInt(),
                avgScore = it.avgScore,
                minScore = it.minScore,
                maxScore = it.maxScore,
                stddev = it.stddev,
                outOf = it.outOf,
                passRate = it.passRate,
            )
        }
    }
}
