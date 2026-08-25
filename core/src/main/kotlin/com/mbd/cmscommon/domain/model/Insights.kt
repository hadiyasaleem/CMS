package com.mbd.cmscommon.domain.model

data class SessionOverview(
    val sessionId: String,
    val deptId: String,
    val shift: Session,
    val currentSemester: Int,
    val students: Int,
    val avgCgpa: Double?,
    val avgAttendance: Double?,
)

data class AtRiskStudent(
    val sessionId: String,
    val rollNumber: String,
    val name: String,
    val cgpa: Double?,
    val attendance: Double?,
)

data class ExamStat(
    val sessionId: String,
    val semester: Int,
    val courseCode: String,
    val examType: ExamType,
    val entered: Int,
    val avgScore: Double?,
    val minScore: Int?,
    val maxScore: Int?,
    val stddev: Double?,
    val outOf: Int,
    val passRate: Double?,
)
