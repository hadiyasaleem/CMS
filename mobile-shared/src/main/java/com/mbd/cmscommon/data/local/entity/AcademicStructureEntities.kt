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
    val shiftMode: String,
    val currentSemester: Int,
    val isActive: Boolean = true,
    val programType: String = "BS",
    val programName: String?,
    val inchargeEmail: String?,
    val maxStudents: Int,
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
    val courseType: String = "MAJOR",
    val isElective: Boolean = false,
    val outline: String?,
    val createdAt: Long = 0L,
    val createdBy: String? = null,
    val updatedAt: Long = 0L,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val deletedBy: String? = null,
)

/** The reusable, college-wide course pool (subject_pool), cached locally for an "add from pool" picker. */
@Entity(tableName = "subject_pool")
data class PoolSubjectEntity(
    @PrimaryKey val courseCode: String,
    val name: String,
    val creditHours: Int,
    val subjectType: String,
    val courseType: String = "MAJOR",
    val outline: String?,
    val createdAt: Long = 0L,
    val createdBy: String? = null,
    val updatedAt: Long = 0L,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val deletedBy: String? = null,
)

@Entity(tableName = "semester_terms", primaryKeys = ["sessionId", "semester"])
data class SemesterTermEntity(
    val sessionId: String,
    val semester: Int,
    val startDate: String?,
    val endDate: String?,
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
    /** The primary session's row keeps its own remote period id. A "shadow" row -- letting a session that
     * merely shares a merged lecture see it on its own grid -- uses a synthetic "$remotePeriodId::$sessionId"
     * id instead, since Room's primary key must be unique per (period, viewing session). */
    @PrimaryKey val id: String,
    val sessionId: String,
    /** The true remote `timetable_periods.id`. Equal to [id] for the primary row; for a shadow row this is
     * the row that edits/removes must actually target. */
    val remotePeriodId: String = id,
    /** Comma-joined ids of every OTHER session sharing this lecture (empty when unmerged). Carried on both
     * the primary row and every shadow row so any view can render merge chips without an extra join. */
    val linkedSessionIds: String = "",
    val shift: String,
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
    val shift: String,
    val linkedEmail: String?,
    val gpa: Double?,
    val cgpa: Double?,
    val profileJson: String? = null,
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
        Index(value = ["sessionId", "rollNumber", "updatedAt"]),
        Index(value = ["sessionId", "semester", "updatedAt"]),
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
    val createdAt: Long = 0L,
    val createdBy: String? = null,
    val updatedAt: Long = 0L,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val deletedBy: String? = null,
)
