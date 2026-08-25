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
    fun observeTotalStudentCount(): Flow<Int>

    suspend fun createSession(deptId: String, startYear: Int, shift: Session): AcademicSession
    suspend fun setCurrentSemester(sessionId: String, semester: Int)
    suspend fun updateSessionDetails(sessionId: String, programName: String?, inchargeEmail: String?, maxStudents: Int)
    suspend fun deleteSession(sessionId: String)
    suspend fun addStudent(sessionId: String, rollNumber: String, name: String, gpa: Double? = null, cgpa: Double? = null)
    suspend fun deleteStudent(studentId: String)
    suspend fun getStudentProfile(sessionId: String, rollNumber: String): StudentProfile?
    suspend fun saveStudentProfile(profile: StudentProfile)
    suspend fun syncSessionsForDept(deptId: String)
    suspend fun syncStudents(sessionId: String)
}

interface CurriculumRepository {
    fun observeSemesterSubjects(sessionId: String, semester: Int): Flow<List<SemesterSubject>>
    fun observeSessionSubjects(sessionId: String): Flow<List<SemesterSubject>>

    suspend fun getSemesterTerm(sessionId: String, semester: Int): SemesterTerm?
    suspend fun saveSemesterSubjects(sessionId: String, semester: Int, subjects: List<SemesterSubject>)
    suspend fun saveSemesterTerm(sessionId: String, semester: Int, startDate: LocalDate?, endDate: LocalDate?)
    suspend fun syncSession(sessionId: String)
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
}

interface SessionTimetableRepository {
    fun observeAllForDay(day: DayOfWeek): Flow<List<SessionPeriod>>
    fun observeDay(sessionId: String, day: DayOfWeek): Flow<List<SessionPeriod>>
    fun observeMyPeriods(teacherId: String): Flow<List<SessionPeriod>>
    fun observeWeek(sessionId: String): Flow<List<SessionPeriod>>

    suspend fun removePeriod(period: SessionPeriod)
    suspend fun savePeriod(period: SessionPeriod)
    suspend fun syncSession(sessionId: String)
}

interface SessionFeeRepository {
    suspend fun getSessionFee(sessionId: String): com.mbd.cmscommon.domain.model.SessionFeeStructure?
    suspend fun saveSessionFee(structure: com.mbd.cmscommon.domain.model.SessionFeeStructure, updatedBy: String)
}
