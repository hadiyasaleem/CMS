package com.mbd.cmscommon.domain.model

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import kotlinx.serialization.Serializable

data class AcademicSession(
    val sessionId: String,
    val deptId: String,
    val startYear: Int,
    val endYear: Int,
    val shift: Session,
    val currentSemester: Int,
    val isActive: Boolean = true,
    val programName: String? = null,
    val inchargeEmail: String? = null,
    val maxStudents: Int = MAX_STUDENTS,
    override val createdAt: Instant = Instant.EPOCH,
    override val createdBy: String? = null,
    override val updatedAt: Instant = Instant.EPOCH,
    override val updatedBy: String? = null,
) : BaseEntity() {
    val label: String get() = "$startYear–$endYear"

    companion object {
        const val MAX_STUDENTS = 50
        const val TOTAL_SEMESTERS = 8
        fun buildId(deptId: String, startYear: Int, shift: Session): String =
            "${deptId}_${startYear}_${shift.name}"
    }
}

/** Result of the `promote-session` edge function: either the pointer advanced, or the class graduated at semester 8. */
@Serializable
data class SessionPromotionResult(
    val sessionId: String,
    val graduated: Boolean = false,
    val promotedTo: Int? = null,
    val papersDeleted: Int = 0,
)

data class AttendanceEntry(
    val status: AttendanceStatus,
    val isLate: Boolean = false,
    val remark: String? = null,
)

data class AttendanceTally(
    val rollNumber: String,
    val present: Int,
    val absent: Int,
    val leave: Int,
    val courseCode: String = "",
) {
    val total: Int get() = present + absent + leave
    val percentage: Float get() = if (total == 0) 0f else (present * 100f) / total
}

data class DailyAttendanceMark(
    val rollNumber: String,
    val date: LocalDate,
    val status: AttendanceStatus,
    val isLate: Boolean = false,
    val remark: String? = null,
    val lectureTopic: String? = null,
)

enum class PeriodType {
    LECTURE,
    ZERO,
    BREAK,
}

data class SemesterGpa(
    val sessionId: String,
    val rollNumber: String,
    val semester: Int,
    val gpa: Double,
    val cgpa: Double,
    val termLabel: String? = null,
    val resultStatus: String = "PENDING",
    val classPosition: Int? = null,
    val remarks: String? = null,
    val supplyCourses: List<String> = emptyList(),
    override val createdAt: Instant = Instant.EPOCH,
    override val createdBy: String? = null,
    override val updatedAt: Instant = Instant.EPOCH,
    override val updatedBy: String? = null,
) : BaseEntity()

enum class SubjectType {
    THEORY,
    LAB,
}

data class SessionPeriod(
    val id: String,
    val sessionId: String,
    val day: DayOfWeek,
    val startTime: String,
    val endTime: String,
    val courseCode: String,
    val subjectName: String,
    val teacherId: String,
    val teacherName: String,
    val periodType: PeriodType = PeriodType.LECTURE,
    val creditHours: Int? = null,
    val roomNo: String? = null,
    val building: String? = null,
    val notes: String? = null,
    val effectiveFrom: LocalDate? = null,
    val effectiveTo: LocalDate? = null,
    override val createdAt: Instant = Instant.EPOCH,
    override val createdBy: String? = null,
    override val updatedAt: Instant = Instant.EPOCH,
    override val updatedBy: String? = null,
) : BaseEntity() {
    val timeRange: String get() = "$startTime–$endTime"

    companion object {
        fun buildId(sessionId: String, day: DayOfWeek, startTime: String): String =
            "${sessionId}_${day.name}_$startTime"
    }
}

data class SemesterSubject(
    val sessionId: String,
    val semester: Int,
    val courseCode: String,
    val name: String,
    val creditHours: Int,
    val subjectType: SubjectType = SubjectType.THEORY,
    val isElective: Boolean = false,
    val outline: String? = null,
    override val createdAt: Instant = Instant.EPOCH,
    override val createdBy: String? = null,
    override val updatedAt: Instant = Instant.EPOCH,
    override val updatedBy: String? = null,
) : BaseEntity()

data class SemesterTerm(
    val sessionId: String,
    val semester: Int,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    override val createdAt: Instant = Instant.EPOCH,
    override val createdBy: String? = null,
    override val updatedAt: Instant = Instant.EPOCH,
    override val updatedBy: String? = null,
) : BaseEntity()

data class SessionStudent(
    val id: String,
    val sessionId: String,
    val deptId: String,
    val rollNumber: String,
    val name: String,
    val linkedEmail: String = "",
    val gpa: Double? = null,
    val cgpa: Double? = null,
    override val createdAt: Instant = Instant.EPOCH,
    override val createdBy: String? = null,
    override val updatedAt: Instant = Instant.EPOCH,
    override val updatedBy: String? = null,
) : BaseEntity() {
    companion object {
        fun buildId(sessionId: String, rollNumber: String): String = "${sessionId}_$rollNumber"
    }
}

data class SessionFeeStructure(
    val sessionId: String,
    val cadence: FeeType,
    val heads: List<FeeHead>,
    val academicYear: String? = null,
    val dueDate: String? = null,
    val lateFineNote: String? = null,
    val paymentNote: String? = null,
    override val createdAt: Instant = Instant.EPOCH,
    override val createdBy: String? = null,
    override val updatedAt: Instant = Instant.EPOCH,
    override val updatedBy: String? = null,
) : BaseEntity() {
    val totalAmount: Double get() = heads.sumOf { it.amount }
}

data class SubjectExamScore(
    val courseCode: String,
    val examType: ExamType,
    val score: Int,
    val maxMarks: Int,
    val wasAbsent: Boolean = false,
    val remarks: String? = null,
)
