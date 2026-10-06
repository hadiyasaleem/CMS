package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.util.requireAffected
import com.mbd.cmscommon.util.orThrowValidation
import com.mbd.cmscommon.util.CmsException
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
import com.mbd.cmscommon.data.sync.fetchPagesConcurrently
import com.mbd.cmscommon.data.sync.maxRemoteUpdatedAt
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionPromotionResult
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.domain.model.ShiftMode
import com.mbd.cmscommon.domain.model.ProgramType
import com.mbd.cmscommon.domain.model.StudentProfile
import com.mbd.cmscommon.domain.model.parseShift
import com.mbd.cmscommon.domain.model.parseShiftMode
import com.mbd.cmscommon.domain.model.parseProgramType
import com.mbd.cmscommon.domain.repository.AvailableRollNumber
import com.mbd.cmscommon.domain.model.profilePhotoExtension
import com.mbd.cmscommon.domain.model.profilePhotoUploadError
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.util.EdgeFunctionErrors
import com.mbd.cmscommon.util.FieldValidators
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.Storage
import io.ktor.client.call.body
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AcademicSessionRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val functions: Functions,
    private val storage: Storage,
    private val sessionDao: AcademicSessionDao,
    private val studentDao: SessionStudentDao,
    private val periodDao: SessionPeriodDao,
    private val checkpointStore: SyncCheckpointStore,
    private val sessionManager: SessionManager,
) : AcademicSessionRepository {

    // The edge function reads req.json() with plain JS destructuring (camelCase keys), but the
    // client's global Json serializer rewrites every property to snake_case for Postgrest's sake --
    // @SerialName pins this one request/response pair back to the literal camelCase the function expects.
    @Serializable
    private data class PromoteSessionRequest(@SerialName("sessionId") val sessionId: String)

    private val profileJson = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private fun syncOwnerKey(): String = sessionManager.accountKey ?: SyncCheckpointDefaults.ownerKey("anonymous-local")

    private suspend fun deptOf(sessionId: String): String = sessionDao.getById(sessionId)?.deptId ?: ""

    private fun AcademicSessionDto.toEntity(fallbackDeptId: String): AcademicSessionEntity {
        val resolvedProgramType = parseProgramType(programType) ?: ProgramType.BS
        return AcademicStructureMapper.sessionDomainToEntity(
            AcademicSession(
                sessionId = sessionId ?: "",
                deptId = deptId ?: fallbackDeptId,
                startYear = startYear,
                endYear = endYear,
                shiftMode = parseShiftMode(shiftMode) ?: ShiftMode.MORNING,
                currentSemester = currentSemester.coerceIn(resolvedProgramType.semesterRange),
                isActive = isActive,
                programType = resolvedProgramType,
                programName = programName,
                inchargeEmail = inchargeEmail,
                maxStudents = maxStudents,
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
        shift = shift ?: Session.MORNING.name,
        linkedEmail = linkedEmail,
        gpa = gpa,
        cgpa = cgpa,
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

    override fun observeActiveSessionStudentCount(): Flow<Int> = studentDao.observeActiveSessionStudentCount()

    override suspend fun createSession(
        deptId: String,
        startYear: Int,
        shiftMode: ShiftMode,
        maxStudents: Int,
        programType: ProgramType,
    ): AcademicSession {
        if (maxStudents !in 1..AcademicSession.MAX_CAPACITY) throw CmsException.Validation("Student capacity must be between 1 and ${AcademicSession.MAX_CAPACITY}.", "maxStudents")
        val startSemester = programType.semesterRange.first
        val session = AcademicSession(
            sessionId = AcademicSession.buildId(deptId, startYear, programType),
            deptId = deptId,
            startYear = startYear,
            endYear = programType.endYear(startYear),
            shiftMode = shiftMode,
            currentSemester = startSemester,
            maxStudents = maxStudents,
            programType = programType,
        )
        val dto = AcademicSessionDto(
            sessionId = session.sessionId,
            deptId = deptId,
            startYear = startYear,
            endYear = session.endYear,
            shiftMode = shiftMode.name,
            maxStudents = maxStudents,
            currentSemester = startSemester,
            programType = programType.name,
            isActive = true,
        )
        postgrest.from(SupabaseTables.ACADEMIC_SESSIONS).upsert(dto) { onConflict = "session_id" }
        postgrest.from(SupabaseTables.ACADEMIC_SESSIONS).update({ set("is_deleted", false) }) {
            filter { eq("session_id", session.sessionId) }
        }
        sessionDao.upsert(AcademicStructureMapper.sessionDomainToEntity(session))
        return session
    }

    override suspend fun updateShiftMode(sessionId: String, shiftMode: ShiftMode, maxStudents: Int) {
        if (maxStudents !in 1..AcademicSession.MAX_CAPACITY) throw CmsException.Validation("Student capacity must be between 1 and ${AcademicSession.MAX_CAPACITY}.", "maxStudents")
        // The database (trg_session_shift_change) is the authority: it rejects dropping a shift that still
        // has data, or a capacity that would push an existing roll number out of its shift's block.
        postgrest.from(SupabaseTables.ACADEMIC_SESSIONS).update({
            set("shift_mode", shiftMode.name)
            set("max_students", maxStudents)
        }) {
            filter { eq("session_id", sessionId) }
        }
        sessionDao.getById(sessionId)?.let { cached ->
            sessionDao.upsert(cached.copy(shiftMode = shiftMode.name, maxStudents = maxStudents, updatedAt = System.currentTimeMillis()))
        }
    }

    override suspend fun promoteSession(sessionId: String): SessionPromotionResult {
        val result = EdgeFunctionErrors.translate {
            functions.invoke("promote-session", PromoteSessionRequest(sessionId)).body<SessionPromotionResult>()
        }
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
        val dropLocalCopy: suspend () -> Unit = {
            studentDao.deleteForSession(sessionId)
            periodDao.deleteForSession(sessionId)
            sessionDao.deleteById(sessionId)
        }
        postgrest.from(SupabaseTables.ACADEMIC_SESSIONS).update({ set("is_deleted", true) }) {
            select()
            filter { eq("session_id", sessionId) }
        }.requireAffected(onNone = dropLocalCopy)
        dropLocalCopy()
    }

    override suspend fun addStudent(sessionId: String, rollNumber: String, name: String, shift: Session, gpa: Double?, cgpa: Double?) {
        val cachedSession = sessionDao.getById(sessionId)
        val deptId = cachedSession?.deptId ?: ""
        // Older caches may hold maxStudents = 0 (cap not set yet); treat a non-positive cap as the default.
        val maxStudents = cachedSession?.maxStudents?.takeIf { it > 0 } ?: AcademicSession.MAX_STUDENTS
        // Friendly pre-check of the per-shift seat cap; the database enforces the same rule.
        val mode = parseShiftMode(cachedSession?.shiftMode)
        val shiftCap = if (mode == ShiftMode.BOTH) maxStudents / 2 else maxStudents
        val shiftCount = studentDao.countForSessionShift(sessionId, shift.name)
        if (shiftCount >= shiftCap) {
            throw CmsException.Conflict("The ${shift.name.lowercase().replaceFirstChar { it.uppercase() }} shift is full ($shiftCap students maximum). Raise the session capacity before adding more students.")
        }
        val roll = FieldValidators.normalizeRollNumber(rollNumber)
        val dto = SessionStudentDto(sessionId = sessionId, rollNumber = roll, name = name.trim(), shift = shift.name, gpa = gpa, cgpa = cgpa)
        postgrest.from(SupabaseTables.SESSION_STUDENTS).upsert(dto) { onConflict = "session_id,roll_number" }
        studentDao.upsert(
            SessionStudentEntity(
                id = SessionStudent.buildId(sessionId, roll),
                sessionId = sessionId,
                deptId = deptId,
                rollNumber = roll,
                name = name.trim(),
                shift = shift.name,
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
            select()
            filter {
                eq("session_id", sessionId)
                eq("roll_number", roll)
            }
        }.requireAffected(onNone = { studentDao.deleteById(studentId) })
        studentDao.deleteById(studentId)
    }

    @Serializable
    private data class RollNumberRow(
        @SerialName("roll_number") val rollNumber: String,
        @SerialName("shift") val shift: String? = null,
    )

    override suspend fun getAvailableRollNumbers(sessionId: String): List<AvailableRollNumber> {
        val params = buildJsonObject { put("p_session", sessionId) }
        return postgrest.rpc(SupabaseTables.RPC_AVAILABLE_ROLL_NUMBERS, params).decodeList<RollNumberRow>()
            .map { AvailableRollNumber(it.rollNumber, parseShift(it.shift) ?: Session.MORNING) }
    }

    override suspend fun delinkStudent(sessionId: String, rollNumber: String, reviewedBy: String?) {
        val roll = rollNumber.trim()
        val cached = studentDao.findByRoll(sessionId, roll)
        val linkedEmail = cached?.linkedEmail?.takeIf { it.isNotBlank() }
        if (linkedEmail != null) {
            postgrest.from(SupabaseTables.PROFILES).update({
                set("linked_session_id", null as String?)
                set("linked_roll", null as String?)
            }) {
                filter { eq("email", linkedEmail) }
            }
            // Same reasoning as the relink path in StudentLinkRequestRepositoryImpl.approveRequest:
            // without this, the delinked account's own APPROVED request keeps its LinkRequestScreen
            // stuck showing "approved, refreshing your account" instead of letting them reapply.
            postgrest.from(SupabaseTables.STUDENT_LINK_REQUESTS).update({
                set("status", "REJECTED")
                set("rejection_reason", "This account was delinked by an administrator.")
                set("reviewed_by", reviewedBy)
                set("reviewed_at", Instant.now().toString())
            }) {
                filter {
                    eq("requested_by_email", linkedEmail)
                    eq("status", "APPROVED")
                }
            }
        }
        postgrest.from(SupabaseTables.SESSION_STUDENTS).update({ set("linked_email", "") }) {
            filter {
                eq("session_id", sessionId)
                eq("roll_number", roll)
            }
        }
        cached?.let { studentDao.upsert(it.copy(linkedEmail = "")) }
    }

    override suspend fun getStudentProfile(sessionId: String, rollNumber: String): StudentProfile? =
        studentDao.findByRoll(sessionId, rollNumber)?.toStudentProfile()

    override fun observeAllStudentProfiles(): Flow<List<StudentProfile>> =
        studentDao.observeAllActive().map { rows -> rows.map { it.toStudentProfile() } }

    private fun SessionStudentEntity.toStudentProfile(): StudentProfile {
        val cached = this
        val dto = cached.profileJson?.let { encoded ->
            runCatching { this@AcademicSessionRepositoryImpl.profileJson.decodeFromString<StudentProfileDto>(encoded) }.getOrNull()
        } ?: StudentProfileDto(
            sessionId = cached.sessionId,
            rollNumber = cached.rollNumber,
            name = cached.name,
            shift = cached.shift,
            linkedEmail = cached.linkedEmail,
            gpa = cached.gpa,
            cgpa = cached.cgpa,
        )
        return StudentProfileMapper.dtoToDomain(dto, cached.sessionId, cached.rollNumber, parseShift(cached.shift) ?: Session.MORNING)
    }
    override suspend fun saveStudentProfile(profile: StudentProfile) {
        val dto = StudentProfileDto(
            sessionId = profile.sessionId,
            rollNumber = profile.rollNumber,
            name = profile.name,
            shift = profile.shift.name,
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
                    shift = profile.shift.name,
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
                    gt("updated_at", since)
                }
                order("updated_at", Order.ASCENDING)
                range(offset, offset + PAGE_SIZE - 1)
            }.decodeList<AcademicSessionDto>()
            if (page.isEmpty()) break

            val entities = page.map { it.toEntity(deptId) }
            val (deleted, active) = entities.partition { it.isDeleted }
            sessionDao.applyDelta(active, deleted.map { it.sessionId })
            // A deleted session takes its roster and timetable with it (the server has removed those rows without tombstones).
            deleted.forEach { studentDao.deleteForSession(it.sessionId); periodDao.deleteForSession(it.sessionId) }
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
                    gt("updated_at", since)
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

    override suspend fun syncAllSessions() {
        val ownerKey = syncOwnerKey()
        val scopeKey = SyncCheckpointDefaults.globalScope()
        val checkpoint = checkpointStore.get(ownerKey, SupabaseTables.ACADEMIC_SESSIONS, scopeKey)
        val since = checkpoint?.lastUpdatedAt ?: SyncCheckpointDefaults.EPOCH
        var maxUpdatedAt = since

        fetchPagesConcurrently(
            pageSize = PAGE_SIZE,
            fetchPage = { from, to ->
                postgrest.from(SupabaseTables.ACADEMIC_SESSIONS).select {
                    filter { gt("updated_at", since) }
                    order("updated_at", Order.ASCENDING)
                    range(from, to)
                }.decodeList<AcademicSessionDto>()
            },
        ) { page ->
            val entities = page.map { it.toEntity(it.deptId ?: "") }
            val (deleted, active) = entities.partition { it.isDeleted }
            sessionDao.applyDelta(active, deleted.map { it.sessionId })
            // A deleted session takes its roster and timetable with it (the server has removed those rows without tombstones).
            deleted.forEach { studentDao.deleteForSession(it.sessionId); periodDao.deleteForSession(it.sessionId) }
            maxUpdatedAt = page.maxRemoteUpdatedAt(maxUpdatedAt) { it.updatedAt }
        }

        checkpointStore.upsert(SyncCheckpoint(ownerKey, SupabaseTables.ACADEMIC_SESSIONS, scopeKey, maxUpdatedAt, PgTime.format(Instant.now()) ?: since))
    }

    override suspend fun syncAllStudents() {
        val ownerKey = syncOwnerKey()
        val scopeKey = SyncCheckpointDefaults.globalScope()
        val checkpoint = checkpointStore.get(ownerKey, SupabaseTables.SESSION_STUDENTS, scopeKey)
        val since = checkpoint?.lastUpdatedAt ?: SyncCheckpointDefaults.EPOCH
        var maxUpdatedAt = since

        fetchPagesConcurrently(
            pageSize = PAGE_SIZE,
            fetchPage = { from, to ->
                postgrest.from(SupabaseTables.SESSION_STUDENTS).select {
                    filter { gt("updated_at", since) }
                    order("updated_at", Order.ASCENDING)
                    range(from, to)
                }.decodeList<StudentProfileDto>()
            },
        ) { page ->
            val entities = page.map { dto -> val sid = dto.sessionId ?: ""; dto.toEntity(sid, deptOf(sid)) }
            val (deleted, active) = entities.partition { it.isDeleted }
            studentDao.applyDelta(active, deleted.map { it.id })
            maxUpdatedAt = page.maxRemoteUpdatedAt(maxUpdatedAt) { it.updatedAt }
        }

        checkpointStore.upsert(SyncCheckpoint(ownerKey, SupabaseTables.SESSION_STUDENTS, scopeKey, maxUpdatedAt, PgTime.format(Instant.now()) ?: since))
    }

    override suspend fun uploadStudentPhoto(sessionId: String, rollNumber: String, imageBytes: ByteArray, mimeType: String) {
        profilePhotoUploadError(mimeType, imageBytes).orThrowValidation("photo")
        val path = "students/$sessionId/$rollNumber.${profilePhotoExtension(mimeType)}"
        storage.from(SupabaseTables.BUCKET_PHOTOS).upload(path, imageBytes) { upsert = true }
        postgrest.from(SupabaseTables.SESSION_STUDENTS).update({ set("photo_path", path) }) {
            filter {
                eq("session_id", sessionId)
                eq("roll_number", rollNumber)
            }
        }
        val cached = studentDao.findByRoll(sessionId, rollNumber)
        if (cached != null) {
            val dto = cached.profileJson?.let { encoded ->
                runCatching { profileJson.decodeFromString<StudentProfileDto>(encoded) }.getOrNull()
            } ?: StudentProfileDto(
                sessionId = cached.sessionId,
                rollNumber = cached.rollNumber,
                name = cached.name,
                shift = cached.shift,
                linkedEmail = cached.linkedEmail,
                gpa = cached.gpa,
                cgpa = cached.cgpa,
            )
            studentDao.upsert(
                cached.copy(
                    profileJson = profileJson.encodeToString(dto.copy(photoPath = path)),
                    updatedAt = System.currentTimeMillis(),
                ),
            )
        }
    }

    override suspend fun downloadStudentPhoto(photoPath: String): ByteArray? =
        runCatching { storage.from(SupabaseTables.BUCKET_PHOTOS).downloadAuthenticated(photoPath) }.getOrNull()

    private companion object {
        const val PAGE_SIZE = 500L
    }
}
