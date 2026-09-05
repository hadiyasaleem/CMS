package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.data.local.dao.AcademicSessionDao
import com.mbd.cmscommon.data.local.dao.SessionPeriodDao
import com.mbd.cmscommon.data.local.dao.SessionStudentDao
import com.mbd.cmscommon.data.local.entity.AcademicSessionEntity
import com.mbd.cmscommon.data.local.entity.SessionStudentEntity
import com.mbd.cmscommon.data.mapper.AcademicStructureMapper
import com.mbd.cmscommon.data.mapper.StudentProfileMapper
import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.AcademicSessionDto
import com.mbd.cmscommon.data.remote.dto.SessionStudentDto
import com.mbd.cmscommon.data.remote.dto.StudentProfileDto
import com.mbd.cmscommon.data.sync.SyncCheckpoint
import com.mbd.cmscommon.data.sync.SyncCheckpointDefaults
import com.mbd.cmscommon.data.sync.SyncCheckpointStore
import com.mbd.cmscommon.data.sync.maxRemoteUpdatedAt
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionPromotionResult
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.domain.model.StudentProfile
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.util.FieldValidators
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.ktor.client.call.body
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AcademicSessionRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val functions: Functions,
    private val sessionDao: AcademicSessionDao,
    private val studentDao: SessionStudentDao,
    private val periodDao: SessionPeriodDao,
    private val checkpointStore: SyncCheckpointStore,
    private val sessionManager: SessionManager,
) : AcademicSessionRepository {

    @Serializable
    private data class PromoteSessionRequest(val sessionId: String)

    private val profileJson = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private fun syncOwnerKey(): String = sessionManager.accountKey ?: SyncCheckpointDefaults.ownerKey("anonymous-local")

    private suspend fun deptOf(sessionId: String): String = sessionDao.getById(sessionId)?.deptId ?: ""

    private fun AcademicSessionDto.toEntity(fallbackDeptId: String): AcademicSessionEntity {
        val shift = runCatching { Session.valueOf(this.shift ?: "") }.getOrDefault(Session.MORNING)
        return AcademicStructureMapper.sessionDomainToEntity(
            AcademicSession(
                sessionId = sessionId ?: "",
                deptId = deptId ?: fallbackDeptId,
                startYear = startYear,
                endYear = endYear,
                shift = shift,
                currentSemester = currentSemester.coerceIn(1, 8),
                isActive = isActive,
                programName = programName,
                inchargeEmail = inchargeEmail,
                maxStudents = maxStudents,
                entityId = entityId ?: 0L,
                createdAt = PgTime.parseOrEpoch(createdAt),
                createdBy = createdBy,
                updatedAt = PgTime.parseOrEpoch(updatedAt),
                updatedBy = updatedBy,
            ),
        ).copy(
            isDeleted = isDeleted,
            deletedAt = PgTime.parse(deletedAt)?.toEpochMilli(),
            deletedBy = deletedBy,
        )
    }

    private fun studentLocalId(dto: SessionStudentDto, fallbackSessionId: String): String {
        val sessionId = dto.sessionId?.takeIf { it.isNotBlank() } ?: fallbackSessionId
        return SessionStudent.buildId(sessionId, dto.rollNumber ?: "")
    }

    private fun SessionStudentDto.toEntity(sessionId: String, deptId: String): SessionStudentEntity = SessionStudentEntity(
        id = studentLocalId(this, sessionId),
        sessionId = sessionId,
        deptId = deptId,
        rollNumber = rollNumber ?: "",
        name = name ?: "",
        linkedEmail = linkedEmail,
        gpa = gpa,
        cgpa = cgpa,
        entityId = entityId ?: 0L,
        createdAt = PgTime.parseOrEpoch(createdAt).toEpochMilli(),
        createdBy = createdBy,
        updatedAt = PgTime.parseOrEpoch(updatedAt).toEpochMilli(),
        updatedBy = updatedBy,
        isDeleted = isDeleted,
        deletedAt = PgTime.parse(deletedAt)?.toEpochMilli(),
        deletedBy = deletedBy,
    )

    private fun StudentProfileDto.toEntity(sessionId: String, deptId: String): SessionStudentEntity =
        StudentProfileMapper.rosterDto(this).toEntity(sessionId, deptId).copy(
            profileJson = profileJson.encodeToString(this),
        )

    override fun observeSessionsForDept(deptId: String): Flow<List<AcademicSession>> =
        sessionDao.observeSessionsForDept(deptId).map { rows -> rows.map { AcademicStructureMapper.sessionEntityToDomain(it) } }

    override fun observeAllSessions(): Flow<List<AcademicSession>> =
        sessionDao.observeAllSessions().map { rows -> rows.map { AcademicStructureMapper.sessionEntityToDomain(it) } }

    override fun observeSession(sessionId: String): Flow<AcademicSession?> =
        sessionDao.observeSession(sessionId).map { it?.let { entity -> AcademicStructureMapper.sessionEntityToDomain(entity) } }

    override fun observeStudents(sessionId: String): Flow<List<SessionStudent>> =
        studentDao.observeForSession(sessionId).map { rows -> rows.map { AcademicStructureMapper.studentEntityToDomain(it) } }

    override fun observeTotalStudentCount(): Flow<Int> = studentDao.observeTotalCount()

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
        sessionDao.upsert(AcademicStructureMapper.sessionDomainToEntity(session))
        return session
    }

    override suspend fun promoteSession(sessionId: String): SessionPromotionResult {
        val response = functions.invoke("promote-session", PromoteSessionRequest(sessionId))
        val result = response.body<SessionPromotionResult>()
        val promotedTo = result.promotedTo
        if (result.graduated) {
            sessionDao.setActive(sessionId, false)
        } else if (promotedTo != null) {
            sessionDao.setCurrentSemester(sessionId, promotedTo)
        }
        return result
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
        sessionDao.getById(sessionId)?.let { cached ->
            sessionDao.upsert(
                cached.copy(
                    programName = programName?.trim()?.takeIf { it.isNotBlank() },
                    inchargeEmail = inchargeEmail?.trim()?.takeIf { it.isNotBlank() },
                    maxStudents = clampedMax,
                    updatedAt = System.currentTimeMillis(),
                ),
            )
        }
    }

    override suspend fun deleteSession(sessionId: String) {
        postgrest.from(SupabaseTables.ACADEMIC_SESSIONS).update({ set("is_deleted", true) }) {
            filter { eq("session_id", sessionId) }
        }
        studentDao.deleteForSession(sessionId)
        periodDao.deleteForSession(sessionId)
        sessionDao.deleteById(sessionId)
    }

    override suspend fun addStudent(sessionId: String, rollNumber: String, name: String, gpa: Double?, cgpa: Double?) {
        val deptId = deptOf(sessionId)
        // A freshly created session stores maxStudents = 0 (cap not set yet). Treat a non-positive
        // cap as "unset" and fall back to the default so students can be added before the admin
        // configures a real capacity (otherwise count >= 0 always rejects with "Session is full").
        val maxStudents = sessionDao.getById(sessionId)?.maxStudents?.takeIf { it > 0 } ?: 50
        val count = studentDao.countForSession(sessionId)
        if (count >= maxStudents) {
            error("Session is full ($maxStudents students max).")
        }
        val roll = FieldValidators.normalizeRollNumber(rollNumber)
        val dto = SessionStudentDto(sessionId = sessionId, rollNumber = roll, name = name.trim(), gpa = gpa, cgpa = cgpa)
        postgrest.from(SupabaseTables.SESSION_STUDENTS).upsert(dto) { onConflict = "session_id,roll_number" }
        studentDao.upsert(
            SessionStudentEntity(
                id = SessionStudent.buildId(sessionId, roll),
                sessionId = sessionId,
                deptId = deptId,
                rollNumber = roll,
                name = name.trim(),
                linkedEmail = null,
                gpa = gpa,
                cgpa = cgpa,
            ),
        )
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
        studentDao.deleteById(studentId)
    }

    override suspend fun getStudentProfile(sessionId: String, rollNumber: String): StudentProfile? {
        val cached = studentDao.findByRoll(sessionId, rollNumber) ?: return null
        val dto = cached.profileJson?.let { encoded ->
            runCatching { profileJson.decodeFromString<StudentProfileDto>(encoded) }.getOrNull()
        } ?: StudentProfileDto(
            sessionId = cached.sessionId,
            rollNumber = cached.rollNumber,
            name = cached.name,
            linkedEmail = cached.linkedEmail,
            gpa = cached.gpa,
            cgpa = cached.cgpa,
        )
        return StudentProfileMapper.dtoToDomain(dto, cached.sessionId, cached.rollNumber)
    }
    override suspend fun saveStudentProfile(profile: StudentProfile) {
        val dto = StudentProfileDto(
            sessionId = profile.sessionId,
            rollNumber = profile.rollNumber,
            name = profile.name,
            universityRollNo = profile.universityRollNo,
            registrationNo = profile.registrationNo,
            fatherName = profile.fatherName,
            guardianName = profile.guardianName,
            cnicBform = profile.cnicBform,
            dob = profile.dob,
            gender = profile.gender,
            phone = profile.phone,
            guardianPhone = profile.guardianPhone,
            personalEmail = profile.personalEmail,
            currentAddress = profile.currentAddress,
            permanentAddress = profile.permanentAddress,
            bloodGroup = profile.bloodGroup,
            domicile = profile.domicile,
            religion = profile.religion,
            admissionDate = profile.admissionDate,
            enrollmentStatus = profile.enrollmentStatus,
            emergencyContactName = profile.emergencyContactName,
            emergencyContactRelation = profile.emergencyContactRelation,
            emergencyContactPhone = profile.emergencyContactPhone,
            specialNeeds = profile.specialNeeds,
            isCr = profile.isCr,
            isGr = profile.isGr,
            linkedEmail = profile.linkedEmail,
            gpa = profile.gpa,
            cgpa = profile.cgpa,
            isActive = profile.isActive,
            photoPath = profile.photoPath,
        )
        postgrest.from(SupabaseTables.SESSION_STUDENTS).update(dto) {
            filter {
                eq("session_id", profile.sessionId)
                eq("roll_number", profile.rollNumber)
            }
        }
        val cached = studentDao.findByRoll(profile.sessionId, profile.rollNumber)
        if (cached != null) {
            studentDao.upsert(
                cached.copy(
                    name = profile.name,
                    linkedEmail = profile.linkedEmail,
                    gpa = profile.gpa,
                    cgpa = profile.cgpa,
                    profileJson = profileJson.encodeToString(dto),
                    updatedAt = System.currentTimeMillis(),
                ),
            )
        } else {
            studentDao.upsert(dto.toEntity(profile.sessionId, deptOf(profile.sessionId)))
        }
    }

    override suspend fun syncSessionsForDept(deptId: String) {
        val ownerKey = syncOwnerKey()
        val scopeKey = SyncCheckpointDefaults.scoped("dept" to deptId)
        val checkpoint = checkpointStore.get(ownerKey, SupabaseTables.ACADEMIC_SESSIONS, scopeKey)
        val since = checkpoint?.lastUpdatedAt ?: SyncCheckpointDefaults.EPOCH
        var maxUpdatedAt = since

        var offset = 0L
        while (true) {
            val page = postgrest.from(SupabaseTables.ACADEMIC_SESSIONS).select {
                filter {
                    eq("dept_id", deptId)
                    gte("updated_at", since)
                }
                order("updated_at", Order.ASCENDING)
                range(offset, offset + PAGE_SIZE - 1)
            }.decodeList<AcademicSessionDto>()
            if (page.isEmpty()) break

            val entities = page.map { it.toEntity(deptId) }
            val (deleted, active) = entities.partition { it.isDeleted }
            sessionDao.applyDelta(active, deleted.map { it.sessionId })
            maxUpdatedAt = page.maxRemoteUpdatedAt(maxUpdatedAt) { it.updatedAt }

            if (page.size < PAGE_SIZE) break
            offset += PAGE_SIZE
        }

        checkpointStore.upsert(SyncCheckpoint(ownerKey, SupabaseTables.ACADEMIC_SESSIONS, scopeKey, maxUpdatedAt, PgTime.format(Instant.now()) ?: since))
    }

    override suspend fun syncStudents(sessionId: String) {
        val ownerKey = syncOwnerKey()
        val scopeKey = SyncCheckpointDefaults.scoped("session" to sessionId)
        val deptId = deptOf(sessionId)
        val checkpoint = checkpointStore.get(ownerKey, SupabaseTables.SESSION_STUDENTS, scopeKey)
        val since = checkpoint?.lastUpdatedAt ?: SyncCheckpointDefaults.EPOCH
        var maxUpdatedAt = since

        var offset = 0L
        while (true) {
            val page = postgrest.from(SupabaseTables.SESSION_STUDENTS).select {
                filter {
                    eq("session_id", sessionId)
                    gte("updated_at", since)
                }
                order("updated_at", Order.ASCENDING)
                range(offset, offset + PAGE_SIZE - 1)
            }.decodeList<StudentProfileDto>()
            if (page.isEmpty()) break

            val entities = page.map { it.toEntity(sessionId, deptId) }
            val (deleted, active) = entities.partition { it.isDeleted }
            studentDao.applyDelta(active, deleted.map { it.id })
            maxUpdatedAt = page.maxRemoteUpdatedAt(maxUpdatedAt) { it.updatedAt }

            if (page.size < PAGE_SIZE) break
            offset += PAGE_SIZE
        }

        checkpointStore.upsert(SyncCheckpoint(ownerKey, SupabaseTables.SESSION_STUDENTS, scopeKey, maxUpdatedAt, PgTime.format(Instant.now()) ?: since))
    }

    private companion object {
        const val PAGE_SIZE = 500L
    }
}
