package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.data.mapper.DesktopMarkEditRequestMapper
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.MarkEditRequestDto
import com.mbd.cmscommon.domain.model.ExamType
import com.mbd.cmscommon.domain.model.MarkEditRequest
import com.mbd.cmscommon.domain.repository.MarkEditRequestRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Desktop repos are always-online: no local persistence, every call re-fetches from Postgrest.
 *
 * [MarkEditRequestRepository] exposes no `observe*`/`sync()` methods, so there is nothing for a
 * screen to subscribe to — [cache] is kept purely as a small in-memory memoization of the last
 * "pending requests" fetch (mirroring the template's cache-then-refresh shape) rather than being
 * exposed as a Flow; both getters always re-fetch and refresh it.
 */
@Singleton
class DesktopMarkEditRequestRepository @Inject constructor(
    private val postgrest: Postgrest,
) : MarkEditRequestRepository {

    private val cache = MutableStateFlow<List<MarkEditRequest>>(emptyList())

    override suspend fun getPendingForAssignment(sessionId: String, courseCode: String, examType: ExamType): List<MarkEditRequest> =
        postgrest.from(SupabaseTables.MARK_EDIT_REQUESTS).select {
            filter {
                eq("session_id", sessionId)
                eq("course_code", courseCode)
                eq("exam_type", examType.name)
                eq("status", DesktopMarkEditRequestMapper.STATUS_PENDING)
                eq("is_deleted", false)
            }
        }.decodeList<MarkEditRequestDto>().map { DesktopMarkEditRequestMapper.dtoToDomain(it) }

    override suspend fun getPendingRequests(): List<MarkEditRequest> {
        val rows = postgrest.from(SupabaseTables.MARK_EDIT_REQUESTS).select {
            filter {
                eq("status", DesktopMarkEditRequestMapper.STATUS_PENDING)
                eq("is_deleted", false)
            }
            order("requested_at", Order.ASCENDING)
        }.decodeList<MarkEditRequestDto>().map { DesktopMarkEditRequestMapper.dtoToDomain(it) }
        cache.value = rows
        return rows
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
            status = DesktopMarkEditRequestMapper.STATUS_PENDING,
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
            set("status", DesktopMarkEditRequestMapper.STATUS_APPROVED)
            set("reviewed_by", reviewedBy)
            set("reviewed_at", Instant.now().toString())
        }) {
            filter { eq("id", requestId) }
        }
        cache.value = cache.value.filterNot { it.id == requestId }
    }

    override suspend fun rejectRequest(requestId: String, reviewedBy: String) {
        postgrest.from(SupabaseTables.MARK_EDIT_REQUESTS).update({
            set("status", DesktopMarkEditRequestMapper.STATUS_REJECTED)
            set("reviewed_by", reviewedBy)
            set("reviewed_at", Instant.now().toString())
        }) {
            filter { eq("id", requestId) }
        }
        cache.value = cache.value.filterNot { it.id == requestId }
    }
}
