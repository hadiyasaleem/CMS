package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.data.local.dao.InsightsDao
import com.mbd.cmscommon.data.local.entity.InsightAtRiskStudentEntity
import com.mbd.cmscommon.data.local.entity.InsightExamStatEntity
import com.mbd.cmscommon.data.local.entity.InsightSessionOverviewEntity
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
import java.time.Instant
import javax.inject.Inject

private fun SessionOverviewDto.toEntity(cachedAt: Long): InsightSessionOverviewEntity = InsightSessionOverviewEntity(
    sessionId = sessionId ?: "",
    deptId = deptId ?: "",
    shift = shift ?: "",
    currentSemester = currentSemester,
    students = students.toInt(),
    avgCgpa = avgCgpa,
    avgAttendance = avgAttendance,
    cachedAt = cachedAt,
)

private fun AtRiskStudentDto.toEntity(cachedAt: Long): InsightAtRiskStudentEntity = InsightAtRiskStudentEntity(
    id = "${sessionId}_$rollNumber",
    sessionId = sessionId ?: "",
    rollNumber = rollNumber ?: "",
    name = name ?: "",
    cgpa = cgpa,
    attendance = attendance,
    cachedAt = cachedAt,
)

private fun ExamStatDto.toEntity(cachedAt: Long): InsightExamStatEntity = InsightExamStatEntity(
    id = "${sessionId}_${semester}_${courseCode}_$examType",
    sessionId = sessionId ?: "",
    semester = semester,
    courseCode = courseCode ?: "",
    examType = examType ?: "",
    entered = entered.toInt(),
    avgScore = avgScore,
    minScore = minScore,
    maxScore = maxScore,
    stddev = stddev,
    outOf = outOf,
    passRate = passRate,
    cachedAt = cachedAt,
)

private fun InsightSessionOverviewEntity.toDomain(): SessionOverview = SessionOverview(
    sessionId = sessionId,
    deptId = deptId,
    shift = runCatching { Session.valueOf(shift) }.getOrDefault(Session.MORNING),
    currentSemester = currentSemester,
    students = students,
    avgCgpa = avgCgpa,
    avgAttendance = avgAttendance,
)

private fun InsightAtRiskStudentEntity.toDomain(): AtRiskStudent = AtRiskStudent(sessionId, rollNumber, name, cgpa, attendance)

private fun InsightExamStatEntity.toDomain(): ExamStat = ExamStat(
    sessionId = sessionId,
    semester = semester,
    courseCode = courseCode,
    examType = runCatching { ExamType.valueOf(examType) }.getOrDefault(ExamType.MIDTERM),
    entered = entered,
    avgScore = avgScore,
    minScore = minScore,
    maxScore = maxScore,
    stddev = stddev,
    outOf = outOf,
    passRate = passRate,
)

class InsightsRepositoryLocalImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val insightsDao: InsightsDao,
) : InsightsRepository {

    override suspend fun getSessionOverviews(): List<SessionOverview> {
        runCatching {
            val cachedAt = Instant.now().toEpochMilli()
            val rows = postgrest.from(SupabaseTables.VIEW_SESSION_OVERVIEW).select().decodeList<SessionOverviewDto>()
            insightsDao.replaceSessionOverviews(rows.map { it.toEntity(cachedAt) })
        }
        return insightsDao.getSessionOverviews().map { it.toDomain() }
    }

    override suspend fun getAtRiskStudents(): List<AtRiskStudent> {
        runCatching {
            val cachedAt = Instant.now().toEpochMilli()
            val rows = postgrest.from(SupabaseTables.VIEW_AT_RISK_STUDENTS).select().decodeList<AtRiskStudentDto>()
            insightsDao.replaceAtRiskStudents(rows.map { it.toEntity(cachedAt) })
        }
        return insightsDao.getAtRiskStudents().map { it.toDomain() }
    }

    override suspend fun getExamStats(): List<ExamStat> {
        runCatching {
            val cachedAt = Instant.now().toEpochMilli()
            val rows = postgrest.from(SupabaseTables.VIEW_EXAM_STATS).select().decodeList<ExamStatDto>()
            insightsDao.replaceExamStats(rows.map { it.toEntity(cachedAt) })
        }
        return insightsDao.getExamStats().map { it.toDomain() }
    }
}
