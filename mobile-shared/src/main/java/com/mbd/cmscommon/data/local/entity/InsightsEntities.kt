package com.mbd.cmscommon.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "insight_session_overviews", indices = [Index(value = ["deptId", "sessionId"])])
data class InsightSessionOverviewEntity(
    @PrimaryKey val sessionId: String,
    val deptId: String,
    val shift: String,
    val currentSemester: Int,
    val students: Int,
    val avgCgpa: Double?,
    val avgAttendance: Double?,
    val cachedAt: Long,
)

@Entity(tableName = "insight_at_risk_students", indices = [Index(value = ["sessionId", "rollNumber"])])
data class InsightAtRiskStudentEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val rollNumber: String,
    val name: String,
    val cgpa: Double?,
    val attendance: Double?,
    val cachedAt: Long,
)

@Entity(tableName = "insight_exam_stats", indices = [Index(value = ["sessionId", "semester", "courseCode", "examType"])])
data class InsightExamStatEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val semester: Int,
    val courseCode: String,
    val examType: String,
    val entered: Int,
    val avgScore: Double?,
    val minScore: Int?,
    val maxScore: Int?,
    val stddev: Double?,
    val outOf: Int,
    val passRate: Double?,
    val cachedAt: Long,
)
