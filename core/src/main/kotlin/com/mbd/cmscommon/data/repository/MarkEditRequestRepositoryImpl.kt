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

class MarkEditRequestRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
) : MarkEditRequestRepository {

    override suspend fun getPendingForAssignment(
        sessionId: String,
        courseCode: String,
        examType: ExamType,
    ): List<MarkEditRequest> {
        val rows = postgrest.from(SupabaseTables.MARK_EDIT_REQUESTS).select {
            filter {
                eq("session_id", sessionId)
                eq("course_code", courseCode)
                eq("exam_type", examType.name)
                eq("status", "PENDING")
            }
        }.decodeList<MarkEditRequestDto>()
        return rows.map { it.toDomain() }
    }

    override suspend fun getPendingRequests(): List<MarkEditRequest> {
        val rows = postgrest.from(SupabaseTables.MARK_EDIT_REQUESTS).select {
            filter { eq("status", "PENDING") }
            order("requested_at", Order.ASCENDING)
        }.decodeList<MarkEditRequestDto>()
        return rows.map { it.toDomain() }
    }

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
            status = "PENDING",
            requestedBy = requestedBy,
        )
        postgrest.from(SupabaseTables.MARK_EDIT_REQUESTS).insert(dto)
    }

    override suspend fun approveRequest(requestId: String, reviewedBy: String) {
        val request = postgrest.from(SupabaseTables.MARK_EDIT_REQUESTS).select {
            filter { eq("id", requestId) }
        }.decodeList<MarkEditRequestDto>().first()

        postgrest.from(SupabaseTables.SESSION_MARKS).update({
            set("score", request.requestedScore)
        }) {
            filter {
                eq("session_id", request.sessionId!!)
                eq("semester", request.semester)
                eq("course_code", request.courseCode!!)
                eq("exam_type", request.examType!!)
                eq("roll_number", request.rollNumber!!)
            }
        }

        postgrest.from(SupabaseTables.MARK_EDIT_REQUESTS).update({
            set("status", "APPROVED")
            set("reviewed_by", reviewedBy)
            set("reviewed_at", Instant.now().toString())
        }) {
            filter { eq("id", requestId) }
        }
    }

    override suspend fun rejectRequest(requestId: String, reviewedBy: String) {
        postgrest.from(SupabaseTables.MARK_EDIT_REQUESTS).update({
            set("status", "REJECTED")
            set("reviewed_by", reviewedBy)
            set("reviewed_at", Instant.now().toString())
        }) {
            filter { eq("id", requestId) }
        }
    }

    private fun MarkEditRequestDto.toDomain() = MarkEditRequest(
        id = id.orEmpty(),
        sessionId = sessionId.orEmpty(),
        semester = semester,
        courseCode = courseCode.orEmpty(),
        examType = runCatching { ExamType.valueOf(examType.orEmpty()) }.getOrDefault(ExamType.MIDTERM),
        rollNumber = rollNumber.orEmpty(),
        currentScore = currentScore,
        requestedScore = requestedScore,
        reason = reason,
        status = runCatching { MarkEditStatus.valueOf(status.orEmpty()) }.getOrDefault(MarkEditStatus.PENDING),
        requestedBy = requestedBy,
        reviewedBy = reviewedBy,
        requestedAt = PgTime.parseOrEpoch(requestedAt),
        reviewedAt = PgTime.parse(reviewedAt),
    )
}
