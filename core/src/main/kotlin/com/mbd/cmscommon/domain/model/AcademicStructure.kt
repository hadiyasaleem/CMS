package com.mbd.cmscommon.domain.model

import com.mbd.cmscommon.util.clockDisplay
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class AcademicSession(
    val sessionId: String,
    val deptId: String,
    val startYear: Int,
    val endYear: Int,
    val shiftMode: ShiftMode,
    val currentSemester: Int,
    val isActive: Boolean = true,
    val programName: String? = null,
    val inchargeEmail: String? = null,
    val maxStudents: Int = MAX_STUDENTS,
    val programType: ProgramType = ProgramType.BS,
    override val createdAt: Instant = Instant.EPOCH,
    override val createdBy: String? = null,
    override val updatedAt: Instant = Instant.EPOCH,
    override val updatedBy: String? = null,
) : BaseEntity() {
    val label: String get() = "$startYear–$endYear"

    /** The shifts this session runs, Morning first. */
    val shifts: List<Session> get() = shiftMode.shifts

    /** The valid semester numbers for this session's program type: 1-8 for BS, 5-8 for MA Replacement. */
    val semesterRange: IntRange get() = programType.semesterRange

    fun runs(shift: Session): Boolean = shiftMode.allows(shift)

    companion object {
        /** Default capacity for a single-shift session; a BOTH session defaults to twice this. */
        const val MAX_STUDENTS = 50
        /** Largest capacity the database accepts (academic_sessions_max_students_range). */
        const val MAX_CAPACITY = 200
        const val TOTAL_SEMESTERS = 8

        /** One session per department, intake year and program type: "{deptId}_{startYear}" for BS,
         * "{deptId}_{startYear}_ma" for MA Replacement (enforced by the database). */
        fun buildId(deptId: String, startYear: Int, programType: ProgramType = ProgramType.BS): String =
            if (programType == ProgramType.MA_REPLACEMENT) "${deptId}_${startYear}_ma" else "${deptId}_$startYear"

        /** The max-students value the create form fills in for [mode]: 50 for one shift, 100 for both. */
        fun defaultMaxStudents(mode: ShiftMode): Int = if (mode == ShiftMode.BOTH) MAX_STUDENTS * 2 else MAX_STUDENTS
    }
}

/** Result of the `promote-session` edge function: either the pointer advanced, or the class graduated at semester 8.
 * @SerialName pins these back to the literal camelCase keys the function's `ok({...})` response body uses,
 * overriding the client's global snake_case naming strategy (meant for Postgrest row DTOs, not this). */
@Serializable
data class SessionPromotionResult(
    @SerialName("sessionId") val sessionId: String,
    val graduated: Boolean = false,
    @SerialName("promotedTo") val promotedTo: Int? = null,
    @SerialName("papersDeleted") val papersDeleted: Int = 0,
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

/** PU's own course categories (general education / major / compulsory religious course / interdisciplinary). */
enum class CourseCategory {
    GENERAL,
    MAJOR,
    COMPULSORY,
    INTERDISCIPLINARY,
}

data class SessionPeriod(
    val id: String,
    val sessionId: String,
    val shift: Session,
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
    /** Sessions -- other than [sessionId], the one this object was read for -- that share this exact lecture
     * (a merged lecture, via the database's period_sessions link). Empty for an unmerged period. */
    val linkedSessionIds: Set<String> = emptySet(),
    /** True for the lecture's own row (owned by [sessionId]); false when this object represents another
     * session's merged-in view of a lecture owned elsewhere -- that view is read-only except for unmerging. */
    val isOwnRow: Boolean = true,
) : BaseEntity() {
    val timeRange: String get() = "${clockDisplay(startTime)}–${clockDisplay(endTime)}"
    val isMergedLecture: Boolean get() = linkedSessionIds.isNotEmpty()

    companion object {
        /** One slot per session, shift, day and start time (the database's uq_session_slot). */
        fun buildId(sessionId: String, shift: Session, day: DayOfWeek, startTime: String): String =
            "${sessionId}_${shift.name}_${day.name}_$startTime"
    }
}


/**
 * A semester's link to a course. The course's own definition (name, credit hours, subject type, category,
 * outline) lives once in the reusable [PoolSubject] pool, keyed by [courseCode] -- this row only says that
 * [sessionId]'s [semester] teaches it, and whether it's an elective there. [name]/[creditHours]/[subjectType]/
 * [courseType]/[outline] are the pool's current values, denormalized here for callers that only care about one
 * semester's subjects and don't want to join against the pool themselves.
 */
data class SemesterSubject(
    val sessionId: String,
    val semester: Int,
    val courseCode: String,
    val name: String,
    val creditHours: Int,
    val subjectType: SubjectType = SubjectType.THEORY,
    val courseType: CourseCategory = CourseCategory.MAJOR,
    val isElective: Boolean = false,
    val outline: String? = null,
    override val createdAt: Instant = Instant.EPOCH,
    override val createdBy: String? = null,
    override val updatedAt: Instant = Instant.EPOCH,
    override val updatedBy: String? = null,
) : BaseEntity()

/**
 * One reusable course definition, shared college-wide by [courseCode]. A semester attaches to it (see
 * [SemesterSubject]) rather than copying its fields, so editing a pool subject updates it everywhere it's linked.
 */
data class PoolSubject(
    val courseCode: String,
    val name: String,
    val creditHours: Int,
    val subjectType: SubjectType = SubjectType.THEORY,
    val courseType: CourseCategory = CourseCategory.MAJOR,
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
    val shift: Session,
    val linkedEmail: String = "",
    val gpa: Double? = null,
    val cgpa: Double? = null,
    val photoPath: String? = null,
    override val createdAt: Instant = Instant.EPOCH,
    override val createdBy: String? = null,
    override val updatedAt: Instant = Instant.EPOCH,
    override val updatedBy: String? = null,
) : BaseEntity() {
    companion object {
        fun buildId(sessionId: String, rollNumber: String): String = "${sessionId}_$rollNumber"
    }
}

/** One shift's fee structure within a session; Morning and Evening are configured independently. */
data class SessionFeeStructure(
    val sessionId: String,
    val shift: Session,
    val cadence: FeeType,
    val heads: List<FeeHead>,
    val academicYear: String? = null,
    val dueDate: String? = null,
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
