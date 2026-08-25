package com.mbd.cmscommon.domain.model

import com.mbd.cmscommon.teacher.ResolvedAssignment

enum class RiskSignal {
    ATTENDANCE,
    CGPA,
}

data class TeacherInsightsScope(
    val overviews: List<SessionOverview>,
    val atRisk: List<AtRiskStudent>,
    val examStats: List<ExamStat>,
    val assignedSessions: Int,
    val assignedClasses: Int,
)

data class InsightsDataQuality(
    val duplicateSessionRows: Int,
    val duplicateRiskRows: Int,
    val duplicateExamRows: Int,
    val invalidSessionRows: Int,
    val invalidRiskRows: Int,
    val invalidExamRows: Int,
    val orphanedRows: Int,
) {
    val issueCount: Int get() =
        duplicateSessionRows + duplicateRiskRows + duplicateExamRows +
            invalidSessionRows + invalidRiskRows + invalidExamRows + orphanedRows
}

data class InsightsSummary(
    val sessions: Int,
    val students: Int,
    val atRiskStudents: Int,
    val examEntries: Int,
    val weightedPassRate: Double?,
)

fun canonicalSessionOverviews(rows: List<SessionOverview>): List<SessionOverview> =
    rows.distinctBy { it.sessionId }

fun canonicalAtRiskStudents(rows: List<AtRiskStudent>): List<AtRiskStudent> =
    rows.distinctBy { it.sessionId to it.rollNumber }

fun canonicalExamStats(rows: List<ExamStat>): List<ExamStat> =
    rows.distinctBy { listOf(it.sessionId, it.semester.toString(), it.courseCode.uppercase(), it.examType.name) }

fun reviewReasons(overview: SessionOverview, validSessionIds: Set<String> = emptySet()): List<String> {
    val reasons = mutableListOf<String>()
    if (overview.sessionId.isBlank()) reasons += "Missing session ID"
    if (overview.deptId.isBlank()) reasons += "Missing department"
    if (overview.currentSemester !in 1..8) reasons += "Semester is outside 1-8"
    if (overview.students < 0) reasons += "Student count is negative"
    overview.avgCgpa?.let { if (it !in 0.0..4.0) reasons += "Average CGPA is outside 0-4" }
    overview.avgAttendance?.let { if (it !in 0.0..100.0) reasons += "Attendance is outside 0-100%" }
    if (validSessionIds.isNotEmpty() && overview.sessionId !in validSessionIds) {
        reasons += "Session is missing from academics"
    }
    return reasons
}

fun reviewReasons(student: AtRiskStudent, validSessionIds: Set<String> = emptySet()): List<String> {
    val reasons = mutableListOf<String>()
    if (student.sessionId.isBlank()) reasons += "Missing session ID"
    if (student.rollNumber.isBlank()) reasons += "Missing roll number"
    if (student.name.isBlank()) reasons += "Missing student name"
    student.cgpa?.let { if (it !in 0.0..4.0) reasons += "CGPA is outside 0-4" }
    student.attendance?.let { if (it !in 0.0..100.0) reasons += "Attendance is outside 0-100%" }
    if (validSessionIds.isNotEmpty() && student.sessionId !in validSessionIds) {
        reasons += "Session is missing from academics"
    }
    return reasons
}

fun reviewReasons(stat: ExamStat, validSessionIds: Set<String> = emptySet()): List<String> {
    val reasons = mutableListOf<String>()
    if (stat.sessionId.isBlank()) reasons += "Missing session ID"
    if (stat.courseCode.isBlank()) reasons += "Missing course code"
    if (stat.semester !in 1..8) reasons += "Semester is outside 1-8"
    if (stat.entered < 0) reasons += "Entered count is negative"
    if (stat.outOf <= 0) reasons += "Maximum marks must be positive"
    stat.avgScore?.let { if (it < 0.0 || it > stat.outOf) reasons += "Average score exceeds its range" }

    val minOutOfRange = stat.minScore?.let { it < 0 || it > stat.outOf } ?: false
    if (minOutOfRange) {
        reasons += "Score range exceeds maximum marks"
    } else {
        val maxOutOfRange = stat.maxScore?.let { it < 0 || it > stat.outOf } ?: false
        if (maxOutOfRange) reasons += "Score range exceeds maximum marks"
    }
    if (stat.minScore != null && stat.maxScore != null && stat.minScore > stat.maxScore) {
        reasons += "Minimum score exceeds maximum score"
    }
    stat.passRate?.let { if (it !in 0.0..100.0) reasons += "Pass rate is outside 0-100%" }
    if (validSessionIds.isNotEmpty() && stat.sessionId !in validSessionIds) {
        reasons += "Session is missing from academics"
    }
    return reasons
}

fun insightsDataQuality(
    overviews: List<SessionOverview>,
    atRisk: List<AtRiskStudent>,
    examStats: List<ExamStat>,
    validSessionIds: Set<String> = emptySet(),
): InsightsDataQuality {
    val allSessionIds = overviews.map { it.sessionId }.toSet()
    val knownSessionIds = validSessionIds.ifEmpty { allSessionIds }

    val orphaned = if (validSessionIds.isEmpty()) {
        0
    } else {
        overviews.count { it.sessionId !in knownSessionIds } +
            atRisk.count { it.sessionId !in knownSessionIds } +
            examStats.count { it.sessionId !in knownSessionIds }
    }

    return InsightsDataQuality(
        duplicateSessionRows = overviews.size - canonicalSessionOverviews(overviews).size,
        duplicateRiskRows = atRisk.size - canonicalAtRiskStudents(atRisk).size,
        duplicateExamRows = examStats.size - canonicalExamStats(examStats).size,
        invalidSessionRows = overviews.count { reviewReasons(it).isNotEmpty() },
        invalidRiskRows = atRisk.count { reviewReasons(it).isNotEmpty() },
        invalidExamRows = examStats.count { reviewReasons(it).isNotEmpty() },
        orphanedRows = orphaned,
    )
}

fun scopeTeacherInsights(
    overviews: List<SessionOverview>,
    atRisk: List<AtRiskStudent>,
    examStats: List<ExamStat>,
    assignments: List<ResolvedAssignment>,
): TeacherInsightsScope {
    val classKeys = assignments.map { it.sessionId to it.courseCode.uppercase() }.toSet()
    val sessionIds = classKeys.map { it.first }.toSet()

    return TeacherInsightsScope(
        overviews = overviews.filter { it.sessionId in sessionIds },
        atRisk = atRisk.filter { it.sessionId in sessionIds },
        examStats = examStats.filter { (it.sessionId to it.courseCode.uppercase()) in classKeys },
        assignedSessions = sessionIds.size,
        assignedClasses = classKeys.size,
    )
}

fun riskSignals(student: AtRiskStudent): Set<RiskSignal> = buildSet {
    if (student.attendance != null && student.attendance < 75.0) add(RiskSignal.ATTENDANCE)
    if (student.cgpa != null && student.cgpa < 2.0) add(RiskSignal.CGPA)
}

fun averagePercentage(stat: ExamStat): Double? {
    val avg = stat.avgScore ?: return null
    if (stat.outOf <= 0) return null
    return ((avg / stat.outOf) * 100.0).coerceIn(0.0, 100.0)
}

fun participationRate(stat: ExamStat, enrolledStudents: Int?): Double? {
    val enrolled = enrolledStudents?.takeIf { it > 0 } ?: return null
    return ((stat.entered.toDouble() / enrolled) * 100.0).coerceIn(0.0, 100.0)
}

fun examWeightedPassRate(stats: List<ExamStat>): Double? {
    val valid = stats.filter { it.entered > 0 && it.passRate != null }
    val entries = valid.sumOf { it.entered }
    if (entries == 0) return null
    val weighted = valid.sumOf { it.passRate!! * it.entered }
    return weighted / entries
}

fun insightsSummary(
    overviews: List<SessionOverview>,
    atRisk: List<AtRiskStudent>,
    examStats: List<ExamStat>,
): InsightsSummary = InsightsSummary(
    sessions = overviews.size,
    students = overviews.sumOf { it.students },
    atRiskStudents = canonicalAtRiskStudents(atRisk).size,
    examEntries = examStats.sumOf { it.entered },
    weightedPassRate = examWeightedPassRate(examStats),
)
