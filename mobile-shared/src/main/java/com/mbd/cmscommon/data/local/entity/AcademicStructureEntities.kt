package com.mbd.cmscommon.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "academic_sessions")
data class AcademicSessionEntity(
    @PrimaryKey val sessionId: String,
    val deptId: String,
    val startYear: Int,
    val endYear: Int,
    val shift: String,
    val currentSemester: Int,
    val isActive: Boolean = true,
    val programName: String?,
    val inchargeEmail: String?,
    val maxStudents: Int,
    val entityId: Long = 0L,
    val createdAt: Long = 0L,
    val createdBy: String? = null,
    val updatedAt: Long = 0L,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val deletedBy: String? = null,
)

@Entity(tableName = "semester_subjects")
data class SemesterSubjectEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val semester: Int,
    val courseCode: String,
    val name: String,
    val creditHours: Int,
    val subjectType: String,
    val isElective: Boolean = false,
    val outline: String?,
    val entityId: Long = 0L,
    val createdAt: Long = 0L,
    val createdBy: String? = null,
    val updatedAt: Long = 0L,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val deletedBy: String? = null,
)

@Entity(
    tableName = "session_attendance_rows",
    indices = [
        Index(value = ["sessionId", "courseCode", "date", "rollNumber"]),
        Index(value = ["sessionId", "courseCode", "updatedAt", "entityId"]),
        Index(value = ["sessionId", "updatedAt", "entityId"]),
        Index(value = ["sessionId", "semester"]),
    ],
)
data class SessionAttendanceRowEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val semester: Int,
    val courseCode: String,
    val date: String,
    val rollNumber: String,
    val status: String,
    val teacherEmail: String,
    val isLate: Boolean = false,
    val remark: String?,
    val lectureTopic: String?,
    val recordedAt: Long,
    val entityId: Long = 0L,
    val createdAt: Long = 0L,
    val createdBy: String? = null,
    val updatedAt: Long = 0L,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val deletedBy: String? = null,
)

@Entity(tableName = "session_attendance_tally")
data class SessionAttendanceTallyEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val courseCode: String,
    val rollNumber: String,
    val present: Int,
    val absent: Int,
    val leave: Int,
)

@Entity(tableName = "session_marks")
data class SessionMarkEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val courseCode: String,
    val examType: String,
    val rollNumber: String,
    val score: Int,
    val maxMarks: Int,
    val wasAbsent: Boolean = false,
    val remarks: String?,
    val entityId: Long = 0L,
    val createdAt: Long = 0L,
    val createdBy: String? = null,
    val updatedAt: Long = 0L,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val deletedBy: String? = null,
)

@Entity(tableName = "session_periods")
data class SessionPeriodEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val deptId: String,
    val day: String,
    val startTime: String?,
    val endTime: String?,
    val courseCode: String?,
    val subjectName: String?,
    val teacherId: String?,
    val teacherName: String?,
    val periodType: String,
    val creditHours: Int?,
    val roomNo: String?,
    val building: String?,
    val notes: String?,
    val effectiveFrom: String?,
    val effectiveTo: String?,
    val entityId: Long = 0L,
    val createdAt: Long = 0L,
    val createdBy: String? = null,
    val updatedAt: Long = 0L,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val deletedBy: String? = null,
)

@Entity(tableName = "session_students")
data class SessionStudentEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val deptId: String,
    val rollNumber: String,
    val name: String,
    val linkedEmail: String?,
    val gpa: Double?,
    val cgpa: Double?,
    val profileJson: String? = null,
    val entityId: Long = 0L,
    val createdAt: Long = 0L,
    val createdBy: String? = null,
    val updatedAt: Long = 0L,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val deletedBy: String? = null,
)

@Entity(
    tableName = "student_semester_gpa",
    indices = [
        Index(value = ["sessionId", "rollNumber", "semester"]),
        Index(value = ["sessionId", "semester", "rollNumber"]),
        Index(value = ["sessionId", "rollNumber", "updatedAt", "entityId"]),
        Index(value = ["sessionId", "semester", "updatedAt", "entityId"]),
    ],
)
data class StudentSemesterGpaEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val rollNumber: String,
    val semester: Int,
    val gpa: Double,
    val cgpa: Double,
    val termLabel: String?,
    val resultStatus: String,
    val classPosition: Int?,
    val remarks: String?,
    val supplyCoursesJson: String,
    val entityId: Long = 0L,
    val createdAt: Long = 0L,
    val createdBy: String? = null,
    val updatedAt: Long = 0L,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val deletedBy: String? = null,
)
