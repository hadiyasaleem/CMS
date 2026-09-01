package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.data.mapper.DesktopInsightsMapper
import com.mbd.cmscommon.data.remote.dto.AcademicSessionDto
import com.mbd.cmscommon.data.remote.dto.AtRiskStudentDto
import com.mbd.cmscommon.data.remote.dto.AttendanceRowDto
import com.mbd.cmscommon.data.remote.dto.ExamStatDto
import com.mbd.cmscommon.data.remote.dto.MarkRowDto
import com.mbd.cmscommon.data.remote.dto.SemesterGpaDto
import com.mbd.cmscommon.data.remote.dto.SessionOverviewDto
import com.mbd.cmscommon.data.remote.dto.SessionStudentDto
import com.mbd.cmscommon.domain.model.AtRiskStudent
import com.mbd.cmscommon.domain.model.ExamStat
import com.mbd.cmscommon.domain.model.SessionOverview
import com.mbd.cmscommon.domain.repository.InsightsRepository
import com.mbd.cmsdesktop.data.cache.DesktopBootstrapSnapshotStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

/**
 * Cache-only Insights. Base tables are synchronized by their own repositories; this class only
 * derives report rows from those durable local snapshots and never queries reporting views.
 */
@Singleton
class DesktopInsightsRepository @Inject constructor(
    private val store: DesktopBootstrapSnapshotStore,
) : InsightsRepository {
    override suspend fun sync() {
        val derived = derive()
        store.writeRows(OVERVIEWS_FILE, SessionOverviewDto.serializer(), derived.overviews)
        store.writeRows(AT_RISK_FILE, AtRiskStudentDto.serializer(), derived.atRisk)
        store.writeRows(EXAM_STATS_FILE, ExamStatDto.serializer(), derived.examStats)
    }

    override suspend fun getSessionOverviews(): List<SessionOverview> =
        derive().overviews.map(DesktopInsightsMapper::sessionOverviewToDomain)

    override suspend fun getAtRiskStudents(): List<AtRiskStudent> =
        derive().atRisk.map(DesktopInsightsMapper::atRiskStudentToDomain)

    override suspend fun getExamStats(): List<ExamStat> =
        derive().examStats.map(DesktopInsightsMapper::examStatToDomain)

    private fun derive(): DerivedInsights {
        val sessions = store.readSessions().filterNot(AcademicSessionDto::isDeleted)
        val students = store.readStudents().filterNot(SessionStudentDto::isDeleted)
        val attendance = store.readRows(ATTENDANCE_FILE, AttendanceRowDto.serializer()).filterNot(AttendanceRowDto::isDeleted)
        val marks = store.readRows(MARKS_FILE, MarkRowDto.serializer()).filterNot(MarkRowDto::isDeleted)
        val gpas = store.readRows(GPA_FILE, SemesterGpaDto.serializer()).filterNot(SemesterGpaDto::isDeleted)
        val latestGpa = gpas.groupBy { "${it.sessionId}|${it.rollNumber}" }.mapValues { (_, rows) -> rows.maxByOrNull { it.semester } }
        val attendanceByStudent = attendance.groupBy { "${it.sessionId}|${it.rollNumber}" }.mapValues { (_, rows) ->
            if (rows.isEmpty()) null else rows.count { it.status.equals("PRESENT", true) || it.status.equals("LATE", true) }.toDouble() * 100 / rows.size
        }
        val overviews = sessions.map { session ->
            val sessionId = session.sessionId.orEmpty()
            val roster = students.filter { it.sessionId == sessionId }
            val cgpas = roster.mapNotNull { latestGpa["$sessionId|${it.rollNumber}"]?.cgpa }
            val attendanceRates = roster.mapNotNull { attendanceByStudent["$sessionId|${it.rollNumber}"] }
            SessionOverviewDto(
                sessionId = sessionId,
                deptId = session.deptId.orEmpty(),
                shift = session.shift.orEmpty(),
                currentSemester = session.currentSemester,
                students = roster.size.toLong(),
                avgCgpa = cgpas.takeIf { it.isNotEmpty() }?.average(),
                avgAttendance = attendanceRates.takeIf { it.isNotEmpty() }?.average(),
            )
        }
        val atRisk = students.mapNotNull { student ->
            val sessionId = student.sessionId.orEmpty()
            val roll = student.rollNumber.orEmpty()
            val cgpa = latestGpa["$sessionId|$roll"]?.cgpa ?: student.cgpa
            val attendanceRate = attendanceByStudent["$sessionId|$roll"]
            if ((cgpa != null && cgpa < AT_RISK_CGPA) || (attendanceRate != null && attendanceRate < AT_RISK_ATTENDANCE)) {
                AtRiskStudentDto(sessionId = sessionId, rollNumber = roll, name = student.name.orEmpty(), cgpa = cgpa, attendance = attendanceRate)
            } else null
        }
        val examStats = marks.groupBy { "${it.sessionId}|${it.semester}|${it.courseCode}|${it.examType}" }.map { (_, rows) ->
            val scores = rows.filterNot { it.wasAbsent }.mapNotNull { it.score }
            val mean = scores.takeIf { it.isNotEmpty() }?.average()
            val stddev = mean?.let { average -> sqrt(scores.sumOf { (it - average) * (it - average) } / scores.size) }
            val outOf = rows.maxOfOrNull { it.maxMarks } ?: 0
            ExamStatDto(
                sessionId = rows.first().sessionId.orEmpty(), semester = rows.first().semester,
                courseCode = rows.first().courseCode.orEmpty(), examType = rows.first().examType.orEmpty(),
                entered = scores.size.toLong(), avgScore = mean, minScore = scores.minOrNull(), maxScore = scores.maxOrNull(),
                stddev = stddev, outOf = outOf,
                passRate = if (scores.isEmpty() || outOf <= 0) null else scores.count { it * 100.0 / outOf >= PASS_PERCENT }.toDouble() * 100 / scores.size,
            )
        }
        return DerivedInsights(overviews, atRisk, examStats)
    }

    private data class DerivedInsights(val overviews: List<SessionOverviewDto>, val atRisk: List<AtRiskStudentDto>, val examStats: List<ExamStatDto>)

    private companion object {
        const val ATTENDANCE_FILE = "session-attendance.json"
        const val MARKS_FILE = "session-marks.json"
        const val GPA_FILE = "semester-gpa.json"
        const val OVERVIEWS_FILE = "insights-session-overviews.json"
        const val AT_RISK_FILE = "insights-at-risk.json"
        const val EXAM_STATS_FILE = "insights-exam-stats.json"
        const val AT_RISK_CGPA = 2.0
        const val AT_RISK_ATTENDANCE = 75.0
        const val PASS_PERCENT = 50.0
    }
}