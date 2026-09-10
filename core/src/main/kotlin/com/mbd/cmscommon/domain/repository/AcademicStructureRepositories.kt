package com.mbd.cmscommon.domain.repository

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.AttendanceEntry
import com.mbd.cmscommon.domain.model.AttendanceTally
import com.mbd.cmscommon.domain.model.DailyAttendanceMark
import com.mbd.cmscommon.domain.model.ExamType
import com.mbd.cmscommon.domain.model.SemesterGpa
import com.mbd.cmscommon.domain.model.SemesterSubject
import com.mbd.cmscommon.domain.model.SemesterTerm
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.domain.model.SessionPromotionResult
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.domain.model.StudentProfile
import com.mbd.cmscommon.domain.model.SubjectExamScore
import java.time.DayOfWeek
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface AcademicSessionRepository {
    fun observeSessionsForDept(deptId: String): Flow<List<AcademicSession>>
    fun observeAllSessions(): Flow<List<AcademicSession>>
    fun observeSession(sessionId: String): Flow<AcademicSession?>
    fun observeStudents(sessionId: String): Flow<List<SessionStudent>>
    /** Students enrolled in a currently-active session -- excludes graduated/inactive intakes. */
    fun observeActiveSessionStudentCount(): Flow<Int>

    suspend fun createSession(deptId: String, startYear: Int, shift: Session): AcademicSession
    /** Advances the session by one semester, or graduates the class if it's already on the final semester. */
    suspend fun promoteSession(sessionId: String): SessionPromotionResult
    suspend fun updateSessionDetails(sessionId: String, programName: String?, inchargeEmail: String?, maxStudents: Int)
    suspend fun deleteSession(sessionId: String)
    suspend fun addStudent(sessionId: String, rollNumber: String, name: String, gpa: Double? = null, cgpa: Double? = null)
    suspend fun deleteStudent(studentId: String)
    suspend fun getStudentProfile(sessionId: String, rollNumber: String): StudentProfile?
    /** Roll numbers in this session not yet claimed by a linked Student account -- backs the
     * account-linking form's roll-number picker. Goes through a SECURITY DEFINER RPC (not a plain
     * select) since an unlinked caller has no RLS visibility into session_students otherwise. */
    suspend fun getAvailableRollNumbers(sessionId: String): List<String>
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
}

interface CurriculumRepository {
    fun observeSemesterSubjects(sessionId: String, semester: Int): Flow<List<SemesterSubject>>
    fun observeSessionSubjects(sessionId: String): Flow<List<SemesterSubject>>

    suspend fun getSemesterTerm(sessionId: String, semester: Int): SemesterTerm?
    suspend fun saveSemesterSubject(subject: SemesterSubject)
    suspend fun deleteSemesterSubject(sessionId: String, semester: Int, courseCode: String)
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
    fun observeAllForDay(day: DayOfWeek): Flow<List<SessionPeriod>>
    fun observeDay(sessionId: String, day: DayOfWeek): Flow<List<SessionPeriod>>
    fun observeMyPeriods(teacherId: String): Flow<List<SessionPeriod>>
    fun observeWeek(sessionId: String): Flow<List<SessionPeriod>>

    suspend fun removePeriod(period: SessionPeriod)
    suspend fun savePeriod(period: SessionPeriod)
    suspend fun syncSession(sessionId: String)
    suspend fun syncAll()
}

interface SessionFeeRepository {
    suspend fun getSessionFee(sessionId: String): com.mbd.cmscommon.domain.model.SessionFeeStructure?
    suspend fun syncSession(sessionId: String) = Unit
    suspend fun syncAll() = Unit
    suspend fun saveSessionFee(structure: com.mbd.cmscommon.domain.model.SessionFeeStructure, updatedBy: String)
}
