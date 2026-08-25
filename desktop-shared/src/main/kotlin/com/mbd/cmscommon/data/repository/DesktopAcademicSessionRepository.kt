package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.data.mapper.DesktopAcademicSessionMapper
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.AcademicSessionDto
import com.mbd.cmscommon.data.remote.dto.SessionStudentDto
import com.mbd.cmscommon.data.remote.dto.StudentProfileDto
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.domain.model.StudentProfile
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * Desktop repos are always-online: no local persistence, `sync*()` does a full re-fetch into an
 * in-memory [MutableStateFlow] cache that the `observe*` methods just expose/derive. Every app
 * screen calls `sync*()` explicitly, matching the mobile RefreshBox pattern but without a
 * Room-backed offline cache.
 *
 * Sessions are cached as one flat list (global — [observeSessionsForDept]/[observeAllSessions]
 * simply filter/pass it through); students are cached as a `Map<sessionId, List<SessionStudent>>`
 * since every student-facing method in the interface is scoped to one session. Student profiles
 * are never cached — same as mobile, [getStudentProfile] always hits Postgrest directly.
 */
@Singleton
class DesktopAcademicSessionRepository @Inject constructor(
    private val postgrest: Postgrest,
) : AcademicSessionRepository {

    private val sessionsCache = MutableStateFlow<List<AcademicSession>>(emptyList())
    private val studentsCache = MutableStateFlow<Map<String, List<SessionStudent>>>(emptyMap())

    private fun deptOf(sessionId: String): String = sessionsCache.value.find { it.sessionId == sessionId }?.deptId ?: ""

    override fun observeSessionsForDept(deptId: String): Flow<List<AcademicSession>> =
        sessionsCache.asStateFlow().map { rows -> rows.filter { it.deptId == deptId } }

    override fun observeAllSessions(): Flow<List<AcademicSession>> = sessionsCache.asStateFlow()

    override fun observeSession(sessionId: String): Flow<AcademicSession?> =
        sessionsCache.asStateFlow().map { rows -> rows.find { it.sessionId == sessionId } }

    override fun observeStudents(sessionId: String): Flow<List<SessionStudent>> =
        studentsCache.asStateFlow().map { it[sessionId] ?: emptyList() }

    override fun observeTotalStudentCount(): Flow<Int> =
        studentsCache.asStateFlow().map { it.values.sumOf { students -> students.size } }

    override suspend fun createSession(deptId: String, startYear: Int, shift: Session): AcademicSession {
        val session = AcademicSession(
            sessionId = AcademicSession.buildId(deptId, startYear, shift),
            deptId = deptId,
            startYear = startYear,
            endYear = startYear + 4,
            shift = shift,
            currentSemester = 1,
            isActive = false,
            maxStudents = 0,
        )
        val dto = AcademicSessionDto(
            sessionId = session.sessionId,
            deptId = deptId,
            startYear = startYear,
            endYear = session.endYear,
            shift = shift.name,
            currentSemester = 1,
            isActive = true,
        )
        postgrest.from(SupabaseTables.ACADEMIC_SESSIONS).upsert(dto) { onConflict = "session_id" }
        postgrest.from(SupabaseTables.ACADEMIC_SESSIONS).update({ set("is_deleted", false) }) {
            filter { eq("session_id", session.sessionId) }
        }
        syncSessionsForDept(deptId)
        return sessionsCache.value.find { it.sessionId == session.sessionId } ?: session
    }

    override suspend fun setCurrentSemester(sessionId: String, semester: Int) {
        val clamped = semester.coerceIn(1, 8)
        postgrest.from(SupabaseTables.ACADEMIC_SESSIONS).update({ set("current_semester", clamped) }) {
            filter { eq("session_id", sessionId) }
        }
        syncSessionsForDept(deptOf(sessionId))
    }

    override suspend fun updateSessionDetails(sessionId: String, programName: String?, inchargeEmail: String?, maxStudents: Int) {
        val clampedMax = maxStudents.coerceAtLeast(1)
        postgrest.from(SupabaseTables.ACADEMIC_SESSIONS).update({
            set("program_name", programName?.trim()?.takeIf { it.isNotBlank() })
            set("incharge_email", inchargeEmail?.trim()?.takeIf { it.isNotBlank() })
            set("max_students", clampedMax)
        }) {
            filter { eq("session_id", sessionId) }
        }
        syncSessionsForDept(deptOf(sessionId))
    }

    override suspend fun deleteSession(sessionId: String) {
        postgrest.from(SupabaseTables.ACADEMIC_SESSIONS).update({ set("is_deleted", true) }) {
            filter { eq("session_id", sessionId) }
        }
        sessionsCache.value = sessionsCache.value.filterNot { it.sessionId == sessionId }
        studentsCache.value = studentsCache.value - sessionId
    }

    override suspend fun addStudent(sessionId: String, rollNumber: String, name: String, gpa: Double?, cgpa: Double?) {
        val session = sessionsCache.value.find { it.sessionId == sessionId }
        val maxStudents = session?.maxStudents ?: 50
        val count = studentsCache.value[sessionId]?.size ?: 0
        if (count >= maxStudents) {
            error("Session is full ($maxStudents students max).")
        }
        val roll = rollNumber.trim().uppercase()
        val dto = SessionStudentDto(sessionId = sessionId, rollNumber = roll, name = name.trim(), gpa = gpa, cgpa = cgpa)
        postgrest.from(SupabaseTables.SESSION_STUDENTS).upsert(dto) { onConflict = "session_id,roll_number" }
        syncStudents(sessionId)
    }

    override suspend fun deleteStudent(studentId: String) {
        val sessionId = studentId.substringBeforeLast('_')
        val roll = studentId.substringAfterLast('_')
        postgrest.from(SupabaseTables.SESSION_STUDENTS).update({ set("is_deleted", true) }) {
            filter {
                eq("session_id", sessionId)
                eq("roll_number", roll)
            }
        }
        syncStudents(sessionId)
    }

    override suspend fun getStudentProfile(sessionId: String, rollNumber: String): StudentProfile? {
        val dto = postgrest.from(SupabaseTables.SESSION_STUDENTS).select {
            filter {
                eq("session_id", sessionId)
                eq("roll_number", rollNumber)
            }
        }.decodeList<StudentProfileDto>().firstOrNull() ?: return null
        return DesktopAcademicSessionMapper.profileDtoToDomain(dto, sessionId, rollNumber)
    }

    override suspend fun saveStudentProfile(profile: StudentProfile) {
        val dto = DesktopAcademicSessionMapper.profileDomainToDto(profile)
        postgrest.from(SupabaseTables.SESSION_STUDENTS).update(dto) {
            filter {
                eq("session_id", profile.sessionId)
                eq("roll_number", profile.rollNumber)
            }
        }
        syncStudents(profile.sessionId)
    }

    override suspend fun syncSessionsForDept(deptId: String) {
        val rows = postgrest.from(SupabaseTables.ACADEMIC_SESSIONS).select {
            filter {
                eq("dept_id", deptId)
                eq("is_deleted", false)
            }
            order("start_year", Order.DESCENDING)
        }.decodeList<AcademicSessionDto>()
        val sessions = rows.map { DesktopAcademicSessionMapper.sessionDtoToDomain(it, deptId) }
        sessionsCache.value = sessionsCache.value.filterNot { it.deptId == deptId } + sessions
    }

    override suspend fun syncStudents(sessionId: String) {
        val deptId = deptOf(sessionId)
        val rows = postgrest.from(SupabaseTables.SESSION_STUDENTS).select {
            filter {
                eq("session_id", sessionId)
                eq("is_deleted", false)
            }
            order("roll_number", Order.ASCENDING)
        }.decodeList<SessionStudentDto>()
        val students = rows.map { DesktopAcademicSessionMapper.studentDtoToDomain(it, sessionId, deptId) }
        studentsCache.value = studentsCache.value + (sessionId to students)
    }
}
