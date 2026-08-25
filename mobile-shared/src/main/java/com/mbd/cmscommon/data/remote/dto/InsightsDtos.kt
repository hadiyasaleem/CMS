package com.mbd.cmscommon.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SessionOverviewDto(
    val sessionId: String? = null,
    val deptId: String? = null,
    val shift: String? = null,
    val currentSemester: Int = 0,
    val students: Long = 0L,
    val avgCgpa: Double? = null,
    val avgAttendance: Double? = null,
)

@Serializable
data class AtRiskStudentDto(
    val sessionId: String? = null,
    val rollNumber: String? = null,
    val name: String? = null,
    val cgpa: Double? = null,
    val attendance: Double? = null,
)

@Serializable
data class ExamStatDto(
    val sessionId: String? = null,
    val semester: Int = 0,
    val courseCode: String? = null,
    val examType: String? = null,
    val entered: Long = 0L,
    val avgScore: Double? = null,
    val minScore: Int? = null,
    val maxScore: Int? = null,
    val stddev: Double? = null,
    val outOf: Int = 0,
    val passRate: Double? = null,
)
