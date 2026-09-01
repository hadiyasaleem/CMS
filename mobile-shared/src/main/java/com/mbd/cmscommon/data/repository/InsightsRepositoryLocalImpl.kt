package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.data.local.dao.AcademicSessionDao
import com.mbd.cmscommon.data.local.dao.InsightsDao
import com.mbd.cmscommon.data.local.dao.SessionAttendanceDao
import com.mbd.cmscommon.data.local.dao.SessionMarkDao
import com.mbd.cmscommon.data.local.dao.SessionStudentDao
import com.mbd.cmscommon.data.local.dao.StudentSemesterGpaDao
import com.mbd.cmscommon.data.local.entity.InsightAtRiskStudentEntity
import com.mbd.cmscommon.data.local.entity.InsightExamStatEntity
import com.mbd.cmscommon.data.local.entity.InsightSessionOverviewEntity
import com.mbd.cmscommon.domain.model.AtRiskStudent
import com.mbd.cmscommon.domain.model.ExamStat
import com.mbd.cmscommon.domain.model.ExamType
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionOverview
import com.mbd.cmscommon.domain.repository.InsightsRepository
import java.time.Instant
import javax.inject.Inject
import kotlin.math.sqrt

private fun InsightSessionOverviewEntity.toDomain() = SessionOverview(sessionId, deptId, runCatching { Session.valueOf(shift) }.getOrDefault(Session.MORNING), currentSemester, students, avgCgpa, avgAttendance)
private fun InsightAtRiskStudentEntity.toDomain() = AtRiskStudent(sessionId, rollNumber, name, cgpa, attendance)
private fun InsightExamStatEntity.toDomain() = ExamStat(sessionId, semester, courseCode, runCatching { ExamType.valueOf(examType) }.getOrDefault(ExamType.MIDTERM), entered, avgScore, minScore, maxScore, stddev, outOf, passRate)

/** Cache-only Insights derived from the existing Room base tables. */
class InsightsRepositoryLocalImpl @Inject constructor(
    private val insightsDao: InsightsDao,
    private val sessionDao: AcademicSessionDao,
    private val studentDao: SessionStudentDao,
    private val attendanceDao: SessionAttendanceDao,
    private val markDao: SessionMarkDao,
    private val gpaDao: StudentSemesterGpaDao,
) : InsightsRepository {
    override suspend fun sync() {
        val cachedAt = Instant.now().toEpochMilli()
        val sessions = sessionDao.getAllActive()
        val students = studentDao.getAllActive()
        val attendance = attendanceDao.getAllRows().filterNot { it.isDeleted }
        val marks = markDao.getAllRows().filterNot { it.isDeleted }
        val gpas = gpaDao.getAllRows().filterNot { it.isDeleted }
        val latestGpa = gpas.groupBy { "${it.sessionId}|${it.rollNumber}" }.mapValues { (_, rows) -> rows.maxByOrNull { it.semester } }
        val attendanceByStudent = attendance.groupBy { "${it.sessionId}|${it.rollNumber}" }.mapValues { (_, rows) ->
            rows.count { it.status.equals("PRESENT", true) || it.status.equals("LATE", true) }.toDouble() * 100 / rows.size
        }
        val overviews = sessions.map { session ->
            val roster = students.filter { it.sessionId == session.sessionId }
            val cgpas = roster.mapNotNull { latestGpa["${session.sessionId}|${it.rollNumber}"]?.cgpa }
            val rates = roster.mapNotNull { attendanceByStudent["${session.sessionId}|${it.rollNumber}"] }
            InsightSessionOverviewEntity(session.sessionId, session.deptId, session.shift, session.currentSemester, roster.size, cgpas.takeIf { it.isNotEmpty() }?.average(), rates.takeIf { it.isNotEmpty() }?.average(), cachedAt)
        }
        val atRisk = students.mapNotNull { student ->
            val key = "${student.sessionId}|${student.rollNumber}"
            val cgpa = latestGpa[key]?.cgpa ?: student.cgpa
            val rate = attendanceByStudent[key]
            if ((cgpa != null && cgpa < 2.0) || (rate != null && rate < 75.0)) InsightAtRiskStudentEntity("${student.sessionId}_${student.rollNumber}", student.sessionId, student.rollNumber, student.name, cgpa, rate, cachedAt) else null
        }
        val examStats = marks.groupBy { "${it.sessionId}|${it.courseCode}|${it.examType}" }.map { (_, rows) ->
            val scores = rows.filterNot { it.wasAbsent }.map { it.score }
            val mean = scores.takeIf { it.isNotEmpty() }?.average()
            val stddev = mean?.let { average -> sqrt(scores.sumOf { (it - average) * (it - average) } / scores.size) }
            val outOf = rows.maxOfOrNull { it.maxMarks } ?: 0
            val first = rows.first()
            InsightExamStatEntity("${first.sessionId}_${first.courseCode}_${first.examType}", first.sessionId, sessions.firstOrNull { it.sessionId == first.sessionId }?.currentSemester ?: 1, first.courseCode, first.examType, scores.size, mean, scores.minOrNull(), scores.maxOrNull(), stddev, outOf, if (scores.isEmpty() || outOf <= 0) null else scores.count { it * 100.0 / outOf >= 50.0 }.toDouble() * 100 / scores.size, cachedAt)
        }
        insightsDao.replaceSessionOverviews(overviews)
        insightsDao.replaceAtRiskStudents(atRisk)
        insightsDao.replaceExamStats(examStats)
    }

    override suspend fun getSessionOverviews(): List<SessionOverview> = insightsDao.getSessionOverviews().map { it.toDomain() }
    override suspend fun getAtRiskStudents(): List<AtRiskStudent> = insightsDao.getAtRiskStudents().map { it.toDomain() }
    override suspend fun getExamStats(): List<ExamStat> = insightsDao.getExamStats().map { it.toDomain() }
}