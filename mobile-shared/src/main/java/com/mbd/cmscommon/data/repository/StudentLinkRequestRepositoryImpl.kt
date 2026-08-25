package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.data.local.dao.SessionStudentDao
import com.mbd.cmscommon.data.local.dao.StudentLinkRequestDao
import com.mbd.cmscommon.data.mapper.StudentLinkRequestMapper
import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.AcademicSessionDto
import com.mbd.cmscommon.data.remote.dto.DepartmentDto
import com.mbd.cmscommon.data.remote.dto.StudentLinkRequestDto
import com.mbd.cmscommon.data.sync.SyncCheckpoint
import com.mbd.cmscommon.data.sync.SyncCheckpointDefaults
import com.mbd.cmscommon.data.sync.SyncCheckpointStore
import com.mbd.cmscommon.data.sync.maxRemoteUpdatedAt
import com.mbd.cmscommon.domain.model.StudentLinkRequest
import com.mbd.cmscommon.domain.repository.RosterLinkMatch
import com.mbd.cmscommon.domain.repository.StudentLinkRequestRepository
import com.mbd.cmscommon.util.FieldValidators
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

@Serializable
private data class RollMatchRow(
    val session_id: String = "",
    val roll_number: String = "",
    val linked_email: String = "",
)

class StudentLinkRequestRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val requestDao: StudentLinkRequestDao,
    private val sessionStudentDao: SessionStudentDao,
    private val checkpointStore: SyncCheckpointStore,
    private val sessionManager: SessionManager,
) : StudentLinkRequestRepository {

    private fun syncOwnerKey(): String = sessionManager.accountKey ?: SyncCheckpointDefaults.ownerKey("anonymous-local")

    private suspend fun matchClaim(sessionId: String, roll: String): RollMatchRow? =
        postgrest.from(SupabaseTables.SESSION_STUDENTS).select {
            filter {
                eq("session_id", sessionId)
                eq("roll_number", roll)
            }
            limit(1)
        }.decodeList<RollMatchRow>().firstOrNull()

    override suspend fun rosterHas(sessionId: String?, rollNumber: String): Boolean =
        rosterLinkMatch(sessionId, rollNumber).exists

    override suspend fun rosterLinkMatch(sessionId: String?, rollNumber: String): RosterLinkMatch {
        val normalizedSession = sessionId?.trim() ?: ""
        val normalizedRoll = rollNumber.trim()
        if (normalizedSession.isBlank() || normalizedRoll.isBlank()) return RosterLinkMatch(false)

        val match = matchClaim(normalizedSession, normalizedRoll) ?: return RosterLinkMatch(false)
        return RosterLinkMatch(true, match.linked_email.takeIf { it.isNotBlank() })
    }

    override fun observePendingRequests(): Flow<List<StudentLinkRequest>> =
        requestDao.observePending().map { rows -> rows.map { StudentLinkRequestMapper.entityToDomain(it) } }

    override fun observeRequestsForStudentUid(requestedByUid: String): Flow<List<StudentLinkRequest>> =
        requestDao.observeForRequester(requestedByUid).map { rows -> rows.map { StudentLinkRequestMapper.entityToDomain(it) } }

    override suspend fun sync() {
        val ownerKey = syncOwnerKey()
        val scopeKey = SyncCheckpointDefaults.globalScope()
        val checkpoint = checkpointStore.get(ownerKey, SupabaseTables.STUDENT_LINK_REQUESTS, scopeKey)
        val since = checkpoint?.lastUpdatedAt ?: SyncCheckpointDefaults.EPOCH
        var maxUpdatedAt = since

        var offset = 0L
        while (true) {
            val page = postgrest.from(SupabaseTables.STUDENT_LINK_REQUESTS).select {
                filter { gte("updated_at", since) }
                order("updated_at", Order.ASCENDING)
                range(offset, offset + PAGE_SIZE - 1)
            }.decodeList<StudentLinkRequestDto>()
            if (page.isEmpty()) break

            val entities = page.map { StudentLinkRequestMapper.dtoToEntity(it) }
            val (deleted, active) = entities.partition { it.isDeleted }
            requestDao.applyDelta(active, deleted.map { it.requestId })
            maxUpdatedAt = page.maxRemoteUpdatedAt(maxUpdatedAt) { it.updatedAt }

            if (page.size < PAGE_SIZE) break
            offset += PAGE_SIZE
        }

        checkpointStore.upsert(SyncCheckpoint(ownerKey, SupabaseTables.STUDENT_LINK_REQUESTS, scopeKey, maxUpdatedAt, PgTime.format(Instant.now()) ?: since))
    }

    override suspend fun submitRequest(
        sessionId: String,
        rollNumber: String,
        name: String,
        cnic: String,
        dob: String,
        universityRoll: String?,
        registrationNo: String?,
        message: String?,
        requestedByUid: String?,
    ) {
        require(requestedByUid != null && FieldValidators.emailError(requestedByUid, false) == null) { "A valid account email is required." }
        require(sessionId.isNotBlank()) { "Choose an academic session." }

        val session = postgrest.from(SupabaseTables.ACADEMIC_SESSIONS).select {
            filter { eq("session_id", sessionId.trim()) }
            limit(1)
        }.decodeList<AcademicSessionDto>().firstOrNull() ?: error("The selected academic session is no longer available.")

        val department = postgrest.from(SupabaseTables.DEPARTMENTS).select {
            filter { eq("dept_id", session.deptId ?: "") }
            limit(1)
        }.decodeList<DepartmentDto>().firstOrNull() ?: error("The selected session's department is no longer available.")

        val normalizedRoll = FieldValidators.normalizeRollNumber(rollNumber)
        FieldValidators.rollNumberError(normalizedRoll, department.code, session.startYear)?.let { throw IllegalArgumentException(it) }
        FieldValidators.nameError(name, "Full name")?.let { throw IllegalArgumentException(it) }
        FieldValidators.cnicError(cnic, true)?.let { throw IllegalArgumentException(it) }
        require(FieldValidators.isoDateError(dob, false, "date of birth", latest = LocalDate.now()) == null) { "Choose a valid date of birth." }
        require((universityRoll ?: "").trim().length <= 40) { "University roll number must not exceed 40 characters." }
        require((registrationNo ?: "").trim().length <= 40) { "Registration number must not exceed 40 characters." }
        require((message ?: "").trim().length <= 500) { "Message must not exceed 500 characters." }

        val dto = StudentLinkRequestDto(
            requestedByEmail = requestedByUid,
            rollNumberClaimed = normalizedRoll,
            sessionId = sessionId.trim().takeIf { it.isNotBlank() },
            nameClaimed = name.trim().takeIf { it.isNotBlank() },
            cnicClaimed = cnic.trim().takeIf { it.isNotBlank() },
            dobClaimed = dob.trim().takeIf { it.isNotBlank() },
            universityRollClaimed = universityRoll?.trim()?.takeIf { it.isNotBlank() },
            registrationNoClaimed = registrationNo?.trim()?.takeIf { it.isNotBlank() },
            message = message?.trim()?.takeIf { it.isNotBlank() },
            status = "PENDING",
        )
        val inserted = postgrest.from(SupabaseTables.STUDENT_LINK_REQUESTS).insert(dto) { select() }.decodeList<StudentLinkRequestDto>().first()
        requestDao.upsert(StudentLinkRequestMapper.dtoToEntity(inserted))
    }

    override suspend fun approveRequest(requestId: String, reviewedByUid: String) {
        val request = postgrest.from(SupabaseTables.STUDENT_LINK_REQUESTS).select {
            filter { eq("request_id", requestId) }
        }.decodeList<StudentLinkRequestDto>().first()

        val roll = request.rollNumberClaimed?.trim() ?: ""
        require(roll.isNotBlank()) { "Link request $requestId has no roll number" }
        val requester = request.requestedByEmail ?: ""
        val sessionId = request.sessionId?.trim() ?: ""
        require(sessionId.isNotBlank()) { "Link request $requestId has no session" }

        val match = matchClaim(sessionId, roll)
            ?: error("No student $roll in session $sessionId — add that student to the roster first.")
        val previousEmail = match.linked_email.takeIf { it.isNotBlank() }

        if (previousEmail != null && previousEmail != requester) {
            postgrest.from(SupabaseTables.PROFILES).update({
                set("linked_session_id", null as String?)
                set("linked_roll", null as String?)
            }) {
                filter { eq("email", previousEmail) }
            }
        }

        postgrest.from(SupabaseTables.SESSION_STUDENTS).update({ set("linked_email", requester) }) {
            filter {
                eq("session_id", sessionId)
                eq("roll_number", roll)
            }
        }

        postgrest.from(SupabaseTables.PROFILES).update({
            set("linked_session_id", sessionId)
            set("linked_roll", roll)
        }) {
            filter { eq("email", requester) }
        }

        postgrest.from(SupabaseTables.STUDENT_LINK_REQUESTS).update({
            set("status", "APPROVED")
            set("session_id", sessionId)
            set("reviewed_by", reviewedByUid)
            set("reviewed_at", Instant.now().toString())
        }) {
            filter { eq("request_id", requestId) }
        }

        requestDao.getById(requestId)?.let { existing ->
            requestDao.upsert(existing.copy(status = "APPROVED", reviewedBy = reviewedByUid))
        }
        sessionStudentDao.findByRoll(sessionId, roll)?.let { existing ->
            sessionStudentDao.upsert(existing.copy(linkedEmail = requester))
        }
    }

    override suspend fun rejectRequest(requestId: String, reviewedByUid: String, reason: String?) {
        require((reason ?: "").trim().length <= 500) { "Keep the rejection reason within 500 characters." }

        postgrest.from(SupabaseTables.STUDENT_LINK_REQUESTS).update({
            set("status", "REJECTED")
            set("reviewed_by", reviewedByUid)
            set("reviewed_at", Instant.now().toString())
            set("rejection_reason", reason?.trim()?.takeIf { it.isNotBlank() })
        }) {
            filter { eq("request_id", requestId) }
        }

        requestDao.getById(requestId)?.let { existing ->
            requestDao.upsert(existing.copy(status = "REJECTED", reviewedBy = reviewedByUid, rejectionReason = reason))
        }
    }

    private companion object {
        const val PAGE_SIZE = 500L
    }
}
