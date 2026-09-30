package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.util.requireAffected
import com.mbd.cmscommon.util.orLogCritical
import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.AttendanceEditRequestDto
import com.mbd.cmscommon.domain.model.AttendanceEditRequest
import com.mbd.cmscommon.domain.model.AttendanceStatus
import com.mbd.cmscommon.domain.model.MarkEditStatus
import com.mbd.cmscommon.domain.repository.AttendanceEditRequestRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** Remote-only: requests are low-volume and reviewed online, so there is no Room cache for them. */
class AttendanceEditRequestRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
) : AttendanceEditRequestRepository {

    override suspend fun getPendingFor(
        sessionId: String,
        courseCode: String,
        from: LocalDate,
        to: LocalDate,
    ): List<AttendanceEditRequest> =
        postgrest.from(SupabaseTables.ATTENDANCE_EDIT_REQUESTS).select {
            filter {
                eq("session_id", sessionId)
                eq("course_code", courseCode)
                gte("date", from.toString())
                lte("date", to.toString())
                eq("status", "PENDING")
                eq("is_deleted", false)
            }
        }.decodeList<AttendanceEditRequestDto>().map { it.toDomain() }

    override suspend fun getPendingRequests(): List<AttendanceEditRequest> =
        postgrest.from(SupabaseTables.ATTENDANCE_EDIT_REQUESTS).select {
            filter {
                eq("status", "PENDING")
                eq("is_deleted", false)
            }
            order("requested_at", Order.ASCENDING)
        }.decodeList<AttendanceEditRequestDto>().map { it.toDomain() }

    override suspend fun submitRequest(
        sessionId: String,
        semester: Int,
        courseCode: String,
        date: LocalDate,
        rollNumber: String,
        currentStatus: AttendanceStatus?,
        currentIsLate: Boolean?,
        requestedStatus: AttendanceStatus,
        requestedIsLate: Boolean,
        reason: String?,
    ) {
        // requested_by is left to its DB default (current_email()), which the insert policy also checks.
        postgrest.from(SupabaseTables.ATTENDANCE_EDIT_REQUESTS).insert(
            buildJsonObject {
                put("session_id", sessionId)
                put("semester", semester)
                put("course_code", courseCode)
                put("date", date.toString())
                put("roll_number", rollNumber)
                put("current_status", currentStatus?.name)
                put("current_is_late", currentIsLate)
                put("requested_status", requestedStatus.name)
                put("requested_is_late", requestedIsLate)
                put("reason", reason?.trim()?.takeIf { it.isNotBlank() })
            },
        )
    }

    override suspend fun approveRequest(requestId: String, reviewedBy: String) {
        postgrest.rpc(
            SupabaseTables.RPC_APPROVE_ATTENDANCE_EDIT_REQUEST,
            buildJsonObject {
                put("p_request_id", requestId)
                put("p_reviewed_by", reviewedBy)
            },
        )
    }

    override suspend fun rejectRequest(requestId: String, reviewedBy: String) {
        postgrest.from(SupabaseTables.ATTENDANCE_EDIT_REQUESTS).update({
            set("status", "REJECTED")
            set("reviewed_by", reviewedBy)
            set("reviewed_at", Instant.now().toString())
        }) {
            select()
            filter {
                eq("id", requestId)
                eq("status", "PENDING")
            }
        }.requireAffected("This attendance edit request was already reviewed or removed. Refresh the list.")
    }

    private fun AttendanceEditRequestDto.toDomain() = AttendanceEditRequest(
        id = id.orEmpty(),
        sessionId = sessionId.orEmpty(),
        semester = semester,
        courseCode = courseCode.orEmpty(),
        date = date?.let(LocalDate::parse) ?: LocalDate.EPOCH,
        rollNumber = rollNumber.orEmpty(),
        currentStatus = currentStatus?.let { runCatching { AttendanceStatus.valueOf(it) }.orLogCritical("AttendanceEditRequest.currentStatus") },
        currentIsLate = currentIsLate,
        requestedStatus = runCatching { AttendanceStatus.valueOf(requestedStatus.orEmpty()) }.orLogCritical("AttendanceEditRequest.requestedStatus", AttendanceStatus.PRESENT),
        requestedIsLate = requestedIsLate,
        reason = reason,
        status = runCatching { MarkEditStatus.valueOf(status.orEmpty()) }.orLogCritical("AttendanceEditRequest.status", MarkEditStatus.PENDING),
        requestedBy = requestedBy,
        reviewedBy = reviewedBy,
        requestedAt = PgTime.parseOrEpoch(requestedAt),
        reviewedAt = PgTime.parse(reviewedAt),
    )
}
