package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.util.requireAffected
import com.mbd.cmscommon.util.orThrowValidation
import com.mbd.cmscommon.util.CmsException
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.data.local.dao.AcademicSessionDao
import com.mbd.cmscommon.data.local.dao.DepartmentDao
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
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

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
    private val sessionDao: AcademicSessionDao,
    private val departmentDao: DepartmentDao,
    private val checkpointStore: SyncCheckpointStore,
    private val sessionManager: SessionManager,
) : StudentLinkRequestRepository {

    private fun syncOwnerKey(): String = sessionManager.accountKey ?: SyncCheckpointDefaults.ownerKey("anonymous-local")




    override suspend fun rosterHas(sessionId: String?, rollNumber: String): Boolean =
        rosterLinkMatch(sessionId, rollNumber).exists

    override suspend fun rosterLinkMatch(sessionId: String?, rollNumber: String): RosterLinkMatch {
        val normalizedSession = sessionId?.trim() ?: ""
        val normalizedRoll = rollNumber.trim()
        if (normalizedSession.isBlank() || normalizedRoll.isBlank()) return RosterLinkMatch(false)

        val match = sessionStudentDao.findByRoll(normalizedSession, normalizedRoll) ?: return RosterLinkMatch(false)
        return RosterLinkMatch(true, match.linkedEmail?.takeIf { it.isNotBlank() })
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
                filter { gt("updated_at", since) }
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
        if (requestedByUid == null || FieldValidators.emailError(requestedByUid, false) != null) throw CmsException.Validation("A valid account email is required.")
        if (sessionId.isBlank()) throw CmsException.Validation("Choose an academic session.")

        val session = sessionDao.getById(sessionId.trim())
            ?.takeUnless { it.isDeleted }
            ?: throw CmsException.NotFound("The selected academic session is no longer available.")
        val department = departmentDao.getById(session.deptId)
            ?.takeUnless { it.isDeleted }
            ?: throw CmsException.NotFound("The selected session's department is no longer available.")

        val normalizedRoll = FieldValidators.normalizeRollNumber(rollNumber)
        FieldValidators.rollNumberError(normalizedRoll, department.code, session.startYear).orThrowValidation("rollNumber")
        FieldValidators.nameError(name, "Full name").orThrowValidation("name")
        FieldValidators.cnicError(cnic, true).orThrowValidation("cnic")
        if (FieldValidators.isoDateError(dob, false, "date of birth", latest = LocalDate.now()) != null) throw CmsException.Validation("Choose a valid date of birth.")
        if ((universityRoll ?: "").trim().length > 40) throw CmsException.Validation("University roll number must not exceed 40 characters.")
        if ((registrationNo ?: "").trim().length > 40) throw CmsException.Validation("Registration number must not exceed 40 characters.")
        if ((message ?: "").trim().length > 500) throw CmsException.Validation("Message must not exceed 500 characters.")

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
        val request = requestDao.getById(requestId)
            ?: throw CmsException.NotFound("This link request is not available on this device yet. Refresh and try again.")

        val roll = request.rollNumberClaimed?.trim() ?: ""
        if (roll.isBlank()) throw CmsException.Validation("This link request has no roll number, so it cannot be approved. Ask the student to submit it again.")
        val requester = request.requestedByUid
        val sessionId = request.sessionIdClaimed?.trim() ?: ""
        if (sessionId.isBlank()) throw CmsException.Validation("This link request has no session selected, so it cannot be approved. Ask the student to submit it again.")

        // Delegated to a security-definer RPC: session_students and profiles are otherwise
        // admin-only tables, so a permitted-but-non-admin teacher's direct writes to them were
        // silently no-op'ing under RLS while the request itself still flipped to APPROVED. The RPC
        // performs the previous-holder unlink, roster link, profile link, and status update
        // atomically, with its own PENDING guard against double-approval.
        postgrest.rpc(
            SupabaseTables.RPC_APPROVE_LINK_REQUEST,
            buildJsonObject {
                put("p_request_id", requestId)
                put("p_reviewed_by", reviewedByUid)
            },
        )

        requestDao.getById(requestId)?.let { existing ->
            requestDao.upsert(existing.copy(status = "APPROVED", reviewedBy = reviewedByUid))
        }
        sessionStudentDao.findByRoll(sessionId, roll)?.let { existing ->
            sessionStudentDao.upsert(existing.copy(linkedEmail = requester))
        }
    }

    override suspend fun rejectRequest(requestId: String, reviewedByUid: String, reason: String?) {
        if ((reason ?: "").trim().length > 500) throw CmsException.Validation("Keep the rejection reason within 500 characters.")

        postgrest.from(SupabaseTables.STUDENT_LINK_REQUESTS).update({
            set("status", "REJECTED")
            set("reviewed_by", reviewedByUid)
            set("reviewed_at", Instant.now().toString())
            set("rejection_reason", reason?.trim()?.takeIf { it.isNotBlank() })
        }) {
            select()
            filter { eq("request_id", requestId) }
        }.requireAffected("This link request was already handled or you no longer have permission to reject it. Refresh the list.")

        requestDao.getById(requestId)?.let { existing ->
            requestDao.upsert(existing.copy(status = "REJECTED", reviewedBy = reviewedByUid, rejectionReason = reason))
        }
    }

    private companion object {
        const val PAGE_SIZE = 500L
    }
}
