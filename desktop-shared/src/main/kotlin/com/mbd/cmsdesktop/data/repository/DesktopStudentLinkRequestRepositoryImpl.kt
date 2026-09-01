package com.mbd.cmsdesktop.data.repository

import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.data.mapper.DesktopStudentLinkRequestMapper
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.StudentLinkRequestDto
import com.mbd.cmscommon.data.sync.SyncCheckpointDefaults
import com.mbd.cmscommon.data.sync.fetchIncrementalDelta
import com.mbd.cmscommon.data.sync.mergeIncrementalDelta
import com.mbd.cmscommon.domain.model.LinkRequestStatus
import com.mbd.cmscommon.domain.model.StudentLinkRequest
import com.mbd.cmscommon.domain.repository.RosterLinkMatch
import com.mbd.cmscommon.domain.repository.StudentLinkRequestRepository
import com.mbd.cmscommon.util.FieldValidators
import com.mbd.cmsdesktop.data.cache.DesktopBootstrapSnapshotStore
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/** Durable cache-first student-link request repository. */
@Singleton
class DesktopStudentLinkRequestRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val store: DesktopBootstrapSnapshotStore,
    private val sessionManager: SessionManager,
) : StudentLinkRequestRepository {

    private val cache = MutableStateFlow(rows().filterNot { it.isDeleted }.map(DesktopStudentLinkRequestMapper::dtoToDomain))

    override suspend fun rosterHas(sessionId: String?, rollNumber: String): Boolean =
        rosterLinkMatch(sessionId, rollNumber).exists

    override suspend fun rosterLinkMatch(sessionId: String?, rollNumber: String): RosterLinkMatch {
        val normalizedSession = sessionId?.trim().orEmpty()
        val normalizedRoll = rollNumber.trim()
        if (normalizedSession.isBlank() || normalizedRoll.isBlank()) return RosterLinkMatch(false)
        val match = store.readStudents().firstOrNull {
            it.sessionId == normalizedSession && it.rollNumber == normalizedRoll && !it.isDeleted
        } ?: return RosterLinkMatch(false)
        return RosterLinkMatch(true, match.linkedEmail?.takeIf { it.isNotBlank() })
    }

    override fun observePendingRequests(): Flow<List<StudentLinkRequest>> =
        cache.asStateFlow().map { list -> list.filter { it.status == LinkRequestStatus.PENDING } }

    override fun observeRequestsForStudentUid(requestedByUid: String): Flow<List<StudentLinkRequest>> =
        cache.asStateFlow().map { list -> list.filter { it.requestedByUid == requestedByUid } }

    override suspend fun sync() {
        val delta = fetchIncrementalDelta(
            store,
            ownerKey(),
            SupabaseTables.STUDENT_LINK_REQUESTS,
            SyncCheckpointDefaults.globalScope(),
            StudentLinkRequestDto::updatedAt,
        ) { since, from, to ->
            postgrest.from(SupabaseTables.STUDENT_LINK_REQUESTS).select {
                filter { gte("updated_at", since) }
                order("updated_at", Order.ASCENDING)
                range(from, to)
            }.decodeList()
        }
        writeMerged(delta)
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
        require(requestedByUid != null && FieldValidators.emailError(requestedByUid, false) == null) {
            "A valid account email is required."
        }
        require(sessionId.isNotBlank()) { "Choose an academic session." }

        val session = store.readSessions().firstOrNull {
            it.sessionId == sessionId.trim() && !it.isDeleted
        } ?: error("The selected academic session is no longer available.")
        val department = store.readDepartments().firstOrNull {
            it.deptId == session.deptId && !it.isDeleted
        } ?: error("The selected session's department is no longer available.")

        val normalizedRoll = FieldValidators.normalizeRollNumber(rollNumber)
        FieldValidators.rollNumberError(normalizedRoll, department.code, session.startYear)?.let {
            throw IllegalArgumentException(it)
        }
        FieldValidators.nameError(name, "Full name")?.let { throw IllegalArgumentException(it) }
        FieldValidators.cnicError(cnic, true)?.let { throw IllegalArgumentException(it) }
        require(FieldValidators.isoDateError(dob, false, "date of birth", latest = LocalDate.now()) == null) {
            "Choose a valid date of birth."
        }
        require((universityRoll ?: "").trim().length <= 40) {
            "University roll number must not exceed 40 characters."
        }
        require((registrationNo ?: "").trim().length <= 40) {
            "Registration number must not exceed 40 characters."
        }
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
        val inserted = postgrest.from(SupabaseTables.STUDENT_LINK_REQUESTS)
            .insert(dto) { select() }
            .decodeList<StudentLinkRequestDto>()
        writeMerged(inserted)
    }

    override suspend fun approveRequest(requestId: String, reviewedByUid: String) {
        val request = rows().firstOrNull { it.requestId == requestId && !it.isDeleted }
            ?: error("Link request $requestId is not available in the local cache.")
        val roll = request.rollNumberClaimed?.trim().orEmpty()
        require(roll.isNotBlank()) { "Link request $requestId has no roll number" }
        val requester = request.requestedByEmail.orEmpty()
        val sessionId = request.sessionId?.trim().orEmpty()
        require(sessionId.isNotBlank()) { "Link request $requestId has no session" }

        val students = store.readStudents()
        val match = students.firstOrNull {
            it.sessionId == sessionId && it.rollNumber == roll && !it.isDeleted
        } ?: error("No student $roll in session $sessionId  add that student to the roster first.")
        val previousEmail = match.linkedEmail?.takeIf { it.isNotBlank() }

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

        store.writeStudents(students.map {
            if (it.sessionId == sessionId && it.rollNumber == roll) it.copy(linkedEmail = requester) else it
        })
        writeMerged(listOf(request.copy(status = "APPROVED", reviewedBy = reviewedByUid)))
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
        rows().firstOrNull { it.requestId == requestId }?.let {
            writeMerged(listOf(it.copy(status = "REJECTED", reviewedBy = reviewedByUid, rejectionReason = reason)))
        }
    }

    private fun rows() = store.readRows(CACHE_FILE, StudentLinkRequestDto.serializer())

    private fun keyOf(dto: StudentLinkRequestDto) = dto.requestId ?: "entity:${dto.entityId}"

    private fun writeMerged(delta: List<StudentLinkRequestDto>) {
        val updated = mergeIncrementalDelta(rows(), delta, ::keyOf, StudentLinkRequestDto::isDeleted)
        store.writeRows(CACHE_FILE, StudentLinkRequestDto.serializer(), updated)
        cache.value = updated.filterNot { it.isDeleted }.map(DesktopStudentLinkRequestMapper::dtoToDomain)
    }

    private fun ownerKey() =
        sessionManager.accountKey ?: SyncCheckpointDefaults.ownerKey("anonymous-local")

    private companion object { const val CACHE_FILE = "student-link-requests.json" }
}
