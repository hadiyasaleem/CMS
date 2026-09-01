package com.mbd.cmsdesktop.data.repository

import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.data.mapper.DesktopAcademicSessionMapper
import com.mbd.cmscommon.data.mapper.StudentProfileMapper
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.AcademicSessionDto
import com.mbd.cmscommon.data.remote.dto.SessionStudentDto
import com.mbd.cmscommon.data.remote.dto.StudentProfileDto
import com.mbd.cmscommon.data.sync.SyncCheckpointDefaults
import com.mbd.cmscommon.data.sync.fetchIncrementalDelta
import com.mbd.cmscommon.data.sync.mergeIncrementalDelta
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.domain.model.StudentProfile
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmsdesktop.data.cache.DesktopBootstrapSnapshotStore
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * Seeded on startup from [DesktopBootstrapSnapshotStore]. Sessions and students are each kept as
 * one flat, unscoped list (mirroring mobile's Room tables): [syncSessionsForDept]/[syncStudents]
 * replace only the slice matching their own scope (filterNot-then-plus) rather than the whole
 * cache, so a narrow sync never evicts data an earlier broader sync had already loaded. Every
 * cache mutation is followed by [persistSessions]/[persistStudents] so the snapshot file tracks
 * the in-memory state. [getStudentProfile]/[saveStudentProfile] are never cached — same as mobile.
 */
@Singleton
class DesktopAcademicSessionRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val snapshotStore: DesktopBootstrapSnapshotStore,
    private val sessionManager: SessionManager,
) : AcademicSessionRepository {

    private val sessionsCache = MutableStateFlow(
        snapshotStore.readSessions().map { DesktopAcademicSessionMapper.sessionDtoToDomain(it) },
    )
    private val studentsCache = MutableStateFlow(
        snapshotStore.readStudents().map { dto ->
            val deptId = sessionsCache.value.find { it.sessionId == dto.sessionId }?.deptId ?: ""
            DesktopAcademicSessionMapper.studentDtoToDomain(dto, dto.sessionId ?: "", deptId)
        },
    )

    private fun deptOf(sessionId: String): String = sessionsCache.value.find { it.sessionId == sessionId }?.deptId ?: ""

    private fun persistSessions() {
        snapshotStore.writeSessions(sessionsCache.value.map { DesktopAcademicSessionMapper.sessionDomainToDto(it) })
    }

    private fun persistStudents() {
        snapshotStore.writeStudents(studentsCache.value.map { DesktopAcademicSessionMapper.studentDomainToDto(it) })
    }

    private fun profileRows() =
        snapshotStore.readRows(PROFILES_CACHE_FILE, StudentProfileDto.serializer())

    private fun writeProfiles(rows: List<StudentProfileDto>) {
        snapshotStore.writeRows(PROFILES_CACHE_FILE, StudentProfileDto.serializer(), rows)
    }

    override fun observeSessionsForDept(deptId: String): Flow<List<AcademicSession>> =
        sessionsCache.asStateFlow().map { rows -> rows.filter { it.deptId == deptId } }

    override fun observeAllSessions(): Flow<List<AcademicSession>> = sessionsCache.asStateFlow()

    override fun observeSession(sessionId: String): Flow<AcademicSession?> =
        sessionsCache.asStateFlow().map { rows -> rows.find { it.sessionId == sessionId } }

    override fun observeStudents(sessionId: String): Flow<List<SessionStudent>> =
        studentsCache.asStateFlow().map { rows -> rows.filter { it.sessionId == sessionId } }

    override fun observeTotalStudentCount(): Flow<Int> = studentsCache.asStateFlow().map { it.size }

    override suspend fun createSession(deptId: String, startYear: Int, shift: Session): AcademicSession {
        val session = AcademicSession(
            sessionId = AcademicSession.buildId(deptId, startYear, shift),
            deptId = deptId,
            startYear = startYear,
            endYear = startYear + 4,
            shift = shift,
            currentSemester = 1,
            isActive = true,
            maxStudents = 0,
        )
        val dto = DesktopAcademicSessionMapper.sessionDomainToDto(session)
        postgrest.from(SupabaseTables.ACADEMIC_SESSIONS).upsert(dto) { onConflict = "session_id" }
        sessionsCache.value = sessionsCache.value.filterNot { it.sessionId == session.sessionId } + session
        persistSessions()
        return session
    }

    override suspend fun setCurrentSemester(sessionId: String, semester: Int) {
        val clamped = semester.coerceIn(1, 8)
        postgrest.from(SupabaseTables.ACADEMIC_SESSIONS).update({ set("current_semester", clamped) }) {
            filter { eq("session_id", sessionId) }
        }
        sessionsCache.value = sessionsCache.value.map {
            if (it.sessionId == sessionId) it.copy(currentSemester = clamped) else it
        }
        persistSessions()
    }

    override suspend fun updateSessionDetails(sessionId: String, programName: String?, inchargeEmail: String?, maxStudents: Int) {
        postgrest.from(SupabaseTables.ACADEMIC_SESSIONS).update({
            set("program_name", programName?.trim()?.takeIf { it.isNotBlank() })
            set("incharge_email", inchargeEmail?.trim()?.takeIf { it.isNotBlank() })
            set("max_students", maxStudents.coerceAtLeast(0))
        }) {
            filter { eq("session_id", sessionId) }
        }
        sessionsCache.value = sessionsCache.value.map {
            if (it.sessionId == sessionId) {
                it.copy(
                    programName = programName?.trim()?.takeIf { value -> value.isNotBlank() },
                    inchargeEmail = inchargeEmail?.trim()?.takeIf { value -> value.isNotBlank() },
                    maxStudents = maxStudents.coerceAtLeast(0),
                )
            } else {
                it
            }
        }
        persistSessions()
    }

    override suspend fun deleteSession(sessionId: String) {
        postgrest.from(SupabaseTables.ACADEMIC_SESSIONS).update({ set("is_deleted", true) }) {
            filter { eq("session_id", sessionId) }
        }
        sessionsCache.value = sessionsCache.value.filterNot { it.sessionId == sessionId }
        studentsCache.value = studentsCache.value.filterNot { it.sessionId == sessionId }
        persistSessions()
        persistStudents()
        writeProfiles(profileRows().filterNot { it.sessionId == sessionId })
    }

    override suspend fun addStudent(sessionId: String, rollNumber: String, name: String, gpa: Double?, cgpa: Double?) {
        val session = sessionsCache.value.find { it.sessionId == sessionId }
        val maxStudents = session?.maxStudents ?: 0
        val count = studentsCache.value.count { it.sessionId == sessionId }
        if (maxStudents > 0 && count >= maxStudents) {
            error("Session is full ($maxStudents students max).")
        }
        val dto = SessionStudentDto(
            sessionId = sessionId,
            rollNumber = rollNumber.trim().uppercase(),
            name = name.trim(),
            gpa = gpa,
            cgpa = cgpa,
        )
        postgrest.from(SupabaseTables.SESSION_STUDENTS).upsert(dto) { onConflict = "session_id,roll_number" }
        val student = DesktopAcademicSessionMapper.studentDtoToDomain(dto, sessionId, deptOf(sessionId))
        studentsCache.value = studentsCache.value.filterNot {
            it.sessionId == sessionId && it.rollNumber == student.rollNumber
        } + student
        persistStudents()
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
        studentsCache.value = studentsCache.value.filterNot {
            it.sessionId == sessionId && it.rollNumber == roll
        }
        persistStudents()
        writeProfiles(profileRows().filterNot { it.sessionId == sessionId && it.rollNumber == roll })
    }

    override suspend fun getStudentProfile(sessionId: String, rollNumber: String): StudentProfile? {
        val dto = profileRows().firstOrNull {
            it.sessionId == sessionId && it.rollNumber == rollNumber && !it.isDeleted
        } ?: return studentsCache.value.firstOrNull {
            it.sessionId == sessionId && it.rollNumber == rollNumber
        }?.let { cached ->
            StudentProfileMapper.dtoToDomain(
                StudentProfileDto(
                    sessionId = cached.sessionId,
                    rollNumber = cached.rollNumber,
                    name = cached.name,
                    linkedEmail = cached.linkedEmail,
                    gpa = cached.gpa,
                    cgpa = cached.cgpa,
                ),
            )
        }
        return StudentProfileMapper.dtoToDomain(dto, sessionId, rollNumber)
    }
    override suspend fun saveStudentProfile(profile: StudentProfile) {
        val dto = DesktopAcademicSessionMapper.profileDomainToDto(profile)
        postgrest.from(SupabaseTables.SESSION_STUDENTS).update(dto) {
            filter {
                eq("session_id", profile.sessionId)
                eq("roll_number", profile.rollNumber)
            }
        }
        writeProfiles(
            mergeIncrementalDelta(
                profileRows(),
                listOf(dto),
                { "${it.sessionId}|${it.rollNumber}" },
                StudentProfileDto::isDeleted,
            ),
        )
        val roster = DesktopAcademicSessionMapper.studentDtoToDomain(
            StudentProfileMapper.rosterDto(dto),
            profile.sessionId,
            deptOf(profile.sessionId),
        )
        studentsCache.value = studentsCache.value.filterNot {
            it.sessionId == profile.sessionId && it.rollNumber == profile.rollNumber
        } + roster
        persistStudents()
    }

    override suspend fun syncSessionsForDept(deptId: String) {
        val delta = fetchIncrementalDelta(
            snapshotStore,
            ownerKey(),
            SupabaseTables.ACADEMIC_SESSIONS,
            SyncCheckpointDefaults.scoped("department" to deptId),
            AcademicSessionDto::updatedAt,
        ) { since, from, to ->
            postgrest.from(SupabaseTables.ACADEMIC_SESSIONS).select {
                filter {
                    eq("dept_id", deptId)
                    gte("updated_at", since)
                }
                order("updated_at", Order.ASCENDING)
                order("session_id", Order.ASCENDING)
                range(from, to)
            }.decodeList()
        }
        val deleted = delta.filter { it.isDeleted }.map { it.sessionId.orEmpty() }.toSet()
        val active = delta.filterNot { it.isDeleted }
        val activeIds = active.map { it.sessionId.orEmpty() }.toSet()
        sessionsCache.value = sessionsCache.value
            .filterNot { it.sessionId in deleted || it.sessionId in activeIds } +
            active.map { DesktopAcademicSessionMapper.sessionDtoToDomain(it, deptId) }
        persistSessions()
    }

    override suspend fun syncStudents(sessionId: String) {
        val deptId = deptOf(sessionId)
        val delta = fetchIncrementalDelta(
            snapshotStore,
            ownerKey(),
            SupabaseTables.SESSION_STUDENTS,
            SyncCheckpointDefaults.scoped("session" to sessionId),
            StudentProfileDto::updatedAt,
        ) { since, from, to ->
            postgrest.from(SupabaseTables.SESSION_STUDENTS).select {
                filter {
                    eq("session_id", sessionId)
                    gte("updated_at", since)
                }
                order("updated_at", Order.ASCENDING)
                order("session_id", Order.ASCENDING)
                range(from, to)
            }.decodeList()
        }
        val deleted = delta.filter { it.isDeleted }.map { it.rollNumber.orEmpty() }.toSet()
        val active = delta.filterNot { it.isDeleted }
        val activeRolls = active.map { it.rollNumber.orEmpty() }.toSet()
        studentsCache.value = studentsCache.value
            .filterNot {
                it.sessionId == sessionId && (it.rollNumber in deleted || it.rollNumber in activeRolls)
            } + active.map { DesktopAcademicSessionMapper.studentDtoToDomain(StudentProfileMapper.rosterDto(it), sessionId, deptId) }
        persistStudents()
        writeProfiles(
            mergeIncrementalDelta(
                profileRows(),
                delta,
                { "${it.sessionId}|${it.rollNumber}" },
                StudentProfileDto::isDeleted,
            ),
        )
    }

    private fun ownerKey() =
        sessionManager.accountKey ?: SyncCheckpointDefaults.ownerKey("anonymous-local")

    private companion object {
        const val PROFILES_CACHE_FILE = "student-profiles.json"
    }
}
