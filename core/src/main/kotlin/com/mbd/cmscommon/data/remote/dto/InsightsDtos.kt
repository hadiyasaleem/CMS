package com.mbd.cmscommon.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SessionOverviewDto(
    val sessionId: String = "",
    val deptId: String = "",
    val shift: String = "MORNING",
    val currentSemester: Int = 1,
    val students: Long = 0L,
    val avgCgpa: Double? = null,
    val avgAttendance: Double? = null,
)

@Serializable
data class AtRiskStudentDto(
    val sessionId: String = "",
    val rollNumber: String = "",
    val name: String = "",
    val cgpa: Double? = null,
    val attendance: Double? = null,
)

@Serializable
data class ExamStatDto(
    val sessionId: String = "",
    val semester: Int = 1,
    val courseCode: String = "",
    val examType: String = "MIDTERM",
    val entered: Long = 0L,
    val avgScore: Double? = null,
    val minScore: Int? = null,
    val maxScore: Int? = null,
    val stddev: Double? = null,
    val outOf: Int = 0,
    val passRate: Double? = null,
)
