package com.mbd.cmscommon.domain.repository

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.AttendanceEntry
import com.mbd.cmscommon.domain.model.AttendanceTally
import com.mbd.cmscommon.domain.model.DailyAttendanceMark
import com.mbd.cmscommon.domain.model.ExamType
import com.mbd.cmscommon.domain.model.PoolSubject
import com.mbd.cmscommon.domain.model.SemesterGpa
import com.mbd.cmscommon.domain.model.SemesterSubject
import com.mbd.cmscommon.domain.model.SemesterTerm
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.domain.model.SessionPromotionResult
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.domain.model.ShiftMode
import com.mbd.cmscommon.domain.model.ProgramType
import com.mbd.cmscommon.domain.model.StudentProfile
import com.mbd.cmscommon.domain.model.SubjectExamScore
import java.time.DayOfWeek
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

/** An unclaimed roll number offered by the account-linking picker, with the shift it belongs to. */
data class AvailableRollNumber(val rollNumber: String, val shift: Session)

interface AcademicSessionRepository {
    fun observeSessionsForDept(deptId: String): Flow<List<AcademicSession>>
    fun observeAllSessions(): Flow<List<AcademicSession>>
    fun observeSession(sessionId: String): Flow<AcademicSession?>
    fun observeStudents(sessionId: String): Flow<List<SessionStudent>>
    /** Students enrolled in a currently-active session -- excludes graduated/inactive intakes. */
    fun observeActiveSessionStudentCount(): Flow<Int>

    /** One session per department + intake year + program type; [maxStudents] defaults to 50 for one shift, 100 for both. */
    suspend fun createSession(
        deptId: String,
        startYear: Int,
        shiftMode: ShiftMode,
        maxStudents: Int = AcademicSession.defaultMaxStudents(shiftMode),
        programType: ProgramType = ProgramType.BS,
    ): AcademicSession
    /** Changes which shifts the session runs (and optionally its capacity). The database refuses to drop a
     * shift that still has students, fees, periods or a datesheet, or to strand a roll number outside its block. */
    suspend fun updateShiftMode(sessionId: String, shiftMode: ShiftMode, maxStudents: Int)
    /** Advances the session by one semester, or graduates the class if it's already on the final semester. */
    suspend fun promoteSession(sessionId: String): SessionPromotionResult
    suspend fun updateSessionDetails(sessionId: String, programName: String?, inchargeEmail: String?, maxStudents: Int)
    suspend fun deleteSession(sessionId: String)
    /** Adds a student to exactly one [shift]; the roll number must sit in that shift's serial block. */
    suspend fun addStudent(sessionId: String, rollNumber: String, name: String, shift: Session, gpa: Double? = null, cgpa: Double? = null)
    suspend fun deleteStudent(studentId: String)
    suspend fun getStudentProfile(sessionId: String, rollNumber: String): StudentProfile?
    /** Every locally cached student across all sessions, with the full profile decoded. */
    fun observeAllStudentProfiles(): Flow<List<StudentProfile>>
    /** Roll numbers in this session not yet claimed by a linked Student account -- backs the
     * account-linking form's roll-number picker. Goes through a SECURITY DEFINER RPC (not a plain
     * select) since an unlinked caller has no RLS visibility into session_students otherwise. */
    suspend fun getAvailableRollNumbers(sessionId: String): List<AvailableRollNumber>
    /** Clears this roster row's linked account (and the corresponding profile's linked_session_id/
     * linked_roll), so it goes back to unlinked and a fresh account-linking claim can be approved
     * for it -- used both for an admin-initiated delink and as the "previous account" side effect
     * of [com.mbd.cmscommon.domain.repository.StudentLinkRequestRepository.approveRequest]. */
    suspend fun delinkStudent(sessionId: String, rollNumber: String, reviewedBy: String? = null)
    suspend fun saveStudentProfile(profile: StudentProfile)
    suspend fun syncSessionsForDept(deptId: String)
    suspend fun syncStudents(sessionId: String)
    /** One delta query across every session's roster instead of one per session -- RLS already
     * restricts the rows a non-admin caller gets back, so this is a strict improvement for every role. */
    suspend fun syncAllSessions()
    suspend fun syncAllStudents()

    /** Uploads a new profile photo, stores it at photos/students/{sessionId}/{rollNumber}.{ext}, and records the path. */
    suspend fun uploadStudentPhoto(sessionId: String, rollNumber: String, imageBytes: ByteArray, mimeType: String)

    /** Downloads a previously-uploaded student photo's bytes, or null if it no longer exists. */
    suspend fun downloadStudentPhoto(photoPath: String): ByteArray?
}

interface CurriculumRepository {
    fun observeSemesterSubjects(sessionId: String, semester: Int): Flow<List<SemesterSubject>>
    fun observeSessionSubjects(sessionId: String): Flow<List<SemesterSubject>>

    /** Every reusable course in the college-wide pool, for an "add from pool" / "copy from" picker. */
    fun observePoolSubjects(): Flow<List<PoolSubject>>

    suspend fun getSemesterTerm(sessionId: String, semester: Int): SemesterTerm?

    /** Creates or edits a course's own definition in the pool, and links it to [subject]'s session+semester. */
    suspend fun saveSemesterSubject(subject: SemesterSubject)

    /** Attaches an existing pool subject to a session+semester without touching the pool's own fields. */
    suspend fun linkSemesterSubject(sessionId: String, semester: Int, courseCode: String, isElective: Boolean)

    /** Removes [courseCode] from [sessionId]'s [semester]; the pool definition (and any other semester's link
     * to it) is untouched. */
    suspend fun deleteSemesterSubject(sessionId: String, semester: Int, courseCode: String)

    /** Links every course [fromSessionId]'s [fromSemester] teaches into [toSessionId]'s [toSemester] too
     * (skipping ones already linked there). */
    suspend fun copySemesterSubjects(fromSessionId: String, fromSemester: Int, toSessionId: String, toSemester: Int)

    /** [copySemesterSubjects], semester 1 through 8, for a brand-new session copying a previous intake's
     * whole curriculum in one go. */
    suspend fun copyAllSemesterSubjects(fromSessionId: String, toSessionId: String) {
        for (semester in 1..8) copySemesterSubjects(fromSessionId, semester, toSessionId, semester)
    }

    suspend fun saveSemesterTerm(sessionId: String, semester: Int, startDate: LocalDate?, endDate: LocalDate?)
    suspend fun syncSession(sessionId: String)
    /** Global delta sync (all sessions in one paginated query) for a full system-wide refresh. */
    suspend fun syncAll()
}

interface SessionAttendanceRepository {
    fun observeStudentTallies(sessionId: String, rollNumber: String): Flow<List<AttendanceTally>>
    fun observeTallies(sessionId: String, courseCode: String): Flow<List<AttendanceTally>>
    fun observeTalliesForSession(sessionId: String): Flow<List<AttendanceTally>>

    suspend fun isMarkedOn(sessionId: String, courseCode: String, date: LocalDate): Boolean
    suspend fun markAttendance(
        sessionId: String,
        courseCode: String,
        date: LocalDate,
        teacherEmail: String,
        entries: Map<String, AttendanceEntry>,
        lectureTopic: String? = null,
    )
    suspend fun marksBetween(sessionId: String, courseCode: String, from: LocalDate, to: LocalDate): List<DailyAttendanceMark>
    suspend fun semesterMarks(sessionId: String, semester: Int): List<DailyAttendanceMark>
    suspend fun syncSession(sessionId: String)
    suspend fun syncSummary(sessionId: String, courseCode: String)
    suspend fun syncAll()
}

interface SessionMarksRepository {
    fun observeAbsentRolls(sessionId: String, courseCode: String, examType: ExamType): Flow<Set<String>>
    fun observeScores(sessionId: String, courseCode: String, examType: ExamType): Flow<Map<String, Int>>
    fun observeStudentMarks(sessionId: String, rollNumber: String): Flow<List<SubjectExamScore>>

    suspend fun getSemesterGpa(sessionId: String, rollNumber: String): List<SemesterGpa>
    suspend fun getSemesterResults(sessionId: String, semester: Int): List<SemesterGpa>
    suspend fun recordSemesterResult(
        sessionId: String,
        rollNumber: String,
        semester: Int,
        gpa: Double,
        cgpa: Double,
        termLabel: String?,
        resultStatus: String,
        classPosition: Int?,
        remarks: String?,
        supplyCourses: List<String>,
    )
    suspend fun saveScores(
        sessionId: String,
        courseCode: String,
        examType: ExamType,
        teacherEmail: String,
        scores: Map<String, Int>,
        absentRolls: Set<String> = emptySet(),
        examDate: LocalDate? = null,
    )
    suspend fun sync(sessionId: String, courseCode: String, examType: ExamType)
    suspend fun syncSession(sessionId: String)
    suspend fun syncAll()
}

interface SessionTimetableRepository {
    /** Every period across every session, for the master timetable's cross-session grids. */
    fun observeAll(): Flow<List<SessionPeriod>>
    fun observeAllForDay(day: DayOfWeek): Flow<List<SessionPeriod>>
    fun observeDay(sessionId: String, day: DayOfWeek): Flow<List<SessionPeriod>>
    fun observeMyPeriods(teacherId: String): Flow<List<SessionPeriod>>
    fun observeWeek(sessionId: String): Flow<List<SessionPeriod>>

    suspend fun removePeriod(period: SessionPeriod)
    suspend fun savePeriod(period: SessionPeriod)
    suspend fun syncSession(sessionId: String)
    suspend fun syncAll()

    /**
     * Merges [sessionId] into [period]'s lecture ([link] = true), or removes it from the merge ([link] =
     * false). Writes one `period_sessions` row and refreshes the local cache for the primary session and
     * [sessionId]. Only meaningful when called against [period]'s own primary session's editor.
     */
    suspend fun setPeriodLink(period: SessionPeriod, sessionId: String, link: Boolean)
}

interface SessionFeeRepository {
    /**
     * One shift's fee structure as the session pays it: the session's own structure, or -- when it has none -- the
     * college-wide base for that shift (marked [com.mbd.cmscommon.domain.model.SessionFeeStructure.inherited]).
     */
    suspend fun getSessionFee(sessionId: String, shift: Session): com.mbd.cmscommon.domain.model.SessionFeeStructure?
    /** The structures this session has set for itself (its overrides); the college base is not included. */
    suspend fun getSessionFees(sessionId: String): List<com.mbd.cmscommon.domain.model.SessionFeeStructure>
    /** Every session's own structures (overrides), for the college-wide overview. */
    suspend fun getAllSessionFees(): List<com.mbd.cmscommon.domain.model.SessionFeeStructure>
    /** Drops the session's own structure for [shift] so it follows the college base again. */
    suspend fun removeSessionFee(sessionId: String, shift: Session, updatedBy: String)

    /** The college-wide base for one shift (Morning and Evening can differ); null until an admin sets it. */
    suspend fun getCollegeFee(shift: Session): com.mbd.cmscommon.domain.model.SessionFeeStructure?
    suspend fun getCollegeFees(): List<com.mbd.cmscommon.domain.model.SessionFeeStructure>
    suspend fun saveCollegeFee(structure: com.mbd.cmscommon.domain.model.SessionFeeStructure, updatedBy: String)
    suspend fun syncSession(sessionId: String) = Unit
    suspend fun syncAll() = Unit
    suspend fun saveSessionFee(structure: com.mbd.cmscommon.domain.model.SessionFeeStructure, updatedBy: String)
}
