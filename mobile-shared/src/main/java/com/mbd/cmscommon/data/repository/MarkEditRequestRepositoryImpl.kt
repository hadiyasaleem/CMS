package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.MarkEditRequestDto
import com.mbd.cmscommon.domain.model.ExamType
import com.mbd.cmscommon.domain.model.MarkEditRequest
import com.mbd.cmscommon.domain.model.MarkEditStatus
import com.mbd.cmscommon.domain.repository.MarkEditRequestRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import java.time.Instant
import javax.inject.Inject

internal const val MARK_EDIT_STATUS_PENDING = "PENDING"
private const val MARK_EDIT_STATUS_APPROVED = "APPROVED"
private const val MARK_EDIT_STATUS_REJECTED = "REJECTED"

internal fun MarkEditRequestDto.toDomain(): MarkEditRequest = MarkEditRequest(
    id = id ?: "",
    sessionId = sessionId ?: "",
    semester = semester,
    courseCode = courseCode ?: "",
    examType = runCatching { ExamType.valueOf(examType ?: "") }.getOrDefault(ExamType.MIDTERM),
    rollNumber = rollNumber ?: "",
    currentScore = currentScore,
    requestedScore = requestedScore,
    reason = reason,
    status = runCatching { MarkEditStatus.valueOf(status ?: "") }.getOrDefault(MarkEditStatus.PENDING),
    requestedBy = requestedBy,
    reviewedBy = reviewedBy,
    requestedAt = PgTime.parseOrEpoch(requestedAt),
    reviewedAt = PgTime.parse(reviewedAt),
    entityId = entityId ?: 0L,
    createdAt = PgTime.parseOrEpoch(createdAt),
    createdBy = createdBy,
    updatedAt = PgTime.parseOrEpoch(updatedAt),
    updatedBy = updatedBy,
)

class MarkEditRequestRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
) : MarkEditRequestRepository {

    override suspend fun getPendingForAssignment(sessionId: String, courseCode: String, examType: ExamType): List<MarkEditRequest> =
        postgrest.from(SupabaseTables.MARK_EDIT_REQUESTS).select {
            filter {
                eq("session_id", sessionId)
                eq("course_code", courseCode)
                eq("exam_type", examType.name)
                eq("status", MARK_EDIT_STATUS_PENDING)
                eq("is_deleted", false)
            }
        }.decodeList<MarkEditRequestDto>().map { it.toDomain() }

    override suspend fun getPendingRequests(): List<MarkEditRequest> =
        postgrest.from(SupabaseTables.MARK_EDIT_REQUESTS).select {
            filter {
                eq("status", MARK_EDIT_STATUS_PENDING)
                eq("is_deleted", false)
            }
            order("requested_at", Order.ASCENDING)
        }.decodeList<MarkEditRequestDto>().map { it.toDomain() }

    override suspend fun submitRequest(
        sessionId: String,
        semester: Int,
        courseCode: String,
        examType: ExamType,
        rollNumber: String,
        currentScore: Int?,
        requestedScore: Int,
        reason: String?,
        requestedBy: String,
    ) {
        val dto = MarkEditRequestDto(
            sessionId = sessionId,
            semester = semester,
            courseCode = courseCode,
            examType = examType.name,
            rollNumber = rollNumber,
            currentScore = currentScore,
            requestedScore = requestedScore,
            reason = reason?.trim()?.takeIf { it.isNotBlank() },
            status = MARK_EDIT_STATUS_PENDING,
            requestedBy = requestedBy,
        )
        postgrest.from(SupabaseTables.MARK_EDIT_REQUESTS).insert(dto)
    }

    override suspend fun approveRequest(requestId: String, reviewedBy: String) {
        val request = postgrest.from(SupabaseTables.MARK_EDIT_REQUESTS).select {
            filter { eq("id", requestId) }
        }.decodeList<MarkEditRequestDto>().first()

        postgrest.from(SupabaseTables.SESSION_MARKS).update({ set("score", request.requestedScore) }) {
            filter {
                eq("session_id", request.sessionId ?: "")
                eq("semester", request.semester)
                eq("course_code", request.courseCode ?: "")
                eq("exam_type", request.examType ?: "")
                eq("roll_number", request.rollNumber ?: "")
            }
        }

        postgrest.from(SupabaseTables.MARK_EDIT_REQUESTS).update({
            set("status", MARK_EDIT_STATUS_APPROVED)
            set("reviewed_by", reviewedBy)
            set("reviewed_at", Instant.now().toString())
        }) {
            filter { eq("id", requestId) }
        }
    }

    override suspend fun rejectRequest(requestId: String, reviewedBy: String) {
        postgrest.from(SupabaseTables.MARK_EDIT_REQUESTS).update({
            set("status", MARK_EDIT_STATUS_REJECTED)
            set("reviewed_by", reviewedBy)
            set("reviewed_at", Instant.now().toString())
        }) {
            filter { eq("id", requestId) }
        }
    }
}
