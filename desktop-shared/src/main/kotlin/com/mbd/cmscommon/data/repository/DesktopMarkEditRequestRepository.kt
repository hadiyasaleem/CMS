package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.data.mapper.DesktopMarkEditRequestMapper
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.MarkEditRequestDto
import com.mbd.cmscommon.data.sync.SyncCheckpointDefaults
import com.mbd.cmscommon.data.sync.fetchIncrementalDelta
import com.mbd.cmscommon.data.sync.mergeIncrementalDelta
import com.mbd.cmscommon.domain.model.ExamType
import com.mbd.cmscommon.domain.model.MarkEditRequest
import com.mbd.cmscommon.domain.repository.MarkEditRequestRepository
import com.mbd.cmsdesktop.data.cache.DesktopBootstrapSnapshotStore
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/** Durable cache-first mark-edit request repository. */
@Singleton
class DesktopMarkEditRequestRepository @Inject constructor(
    private val postgrest: Postgrest,
    private val store: DesktopBootstrapSnapshotStore,
    private val sessionManager: SessionManager,
) : MarkEditRequestRepository {
    override suspend fun sync() {
        val delta = fetchIncrementalDelta(
            store, ownerKey(), SupabaseTables.MARK_EDIT_REQUESTS,
            SyncCheckpointDefaults.globalScope(), MarkEditRequestDto::updatedAt,
        ) { since, from, to ->
            postgrest.from(SupabaseTables.MARK_EDIT_REQUESTS).select {
                filter { gte("updated_at", since) }
                order("updated_at", Order.ASCENDING)
                range(from, to)
            }.decodeList()
        }
        writeMerged(delta)
    }

    override suspend fun getPendingForAssignment(
        sessionId: String,
        courseCode: String,
        examType: ExamType,
    ): List<MarkEditRequest> = pendingRows().filter {
        it.sessionId == sessionId && it.courseCode == courseCode && it.examType == examType.name
    }.map(DesktopMarkEditRequestMapper::dtoToDomain)

    override suspend fun getPendingRequests(): List<MarkEditRequest> =
        pendingRows().map(DesktopMarkEditRequestMapper::dtoToDomain).sortedBy { it.requestedAt }

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
        val inserted = postgrest.from(SupabaseTables.MARK_EDIT_REQUESTS).insert(
            MarkEditRequestDto(
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
            ),
        ) { select() }.decodeList<MarkEditRequestDto>()
        writeMerged(inserted)
    }

    override suspend fun approveRequest(requestId: String, reviewedBy: String) {
        val request = cachedRows().firstOrNull { it.id == requestId }
            ?: error("Mark edit request is not available in the local cache. Refresh and try again.")
        postgrest.from(SupabaseTables.SESSION_MARKS).update({ set("score", request.requestedScore) }) {
            filter {
                eq("session_id", request.sessionId.orEmpty())
                eq("semester", request.semester)
                eq("course_code", request.courseCode.orEmpty())
                eq("exam_type", request.examType.orEmpty())
                eq("roll_number", request.rollNumber.orEmpty())
            }
        }
        updateStatus(requestId, reviewedBy, DesktopMarkEditRequestMapper.STATUS_APPROVED)
    }

    override suspend fun rejectRequest(requestId: String, reviewedBy: String) {
        updateStatus(requestId, reviewedBy, DesktopMarkEditRequestMapper.STATUS_REJECTED)
    }

    private suspend fun updateStatus(requestId: String, reviewedBy: String, status: String) {
        postgrest.from(SupabaseTables.MARK_EDIT_REQUESTS).update({
            set("status", status)
            set("reviewed_by", reviewedBy)
            set("reviewed_at", Instant.now().toString())
        }) { filter { eq("id", requestId) } }
        store.writeRows(CACHE_FILE, MarkEditRequestDto.serializer(), cachedRows().map {
            if (it.id == requestId) it.copy(status = status, reviewedBy = reviewedBy) else it
        })
    }

    private fun pendingRows() = cachedRows().filter {
        !it.isDeleted && it.status == DesktopMarkEditRequestMapper.STATUS_PENDING
    }

    private fun cachedRows() = store.readRows(CACHE_FILE, MarkEditRequestDto.serializer())

    private fun writeMerged(delta: List<MarkEditRequestDto>) {
        store.writeRows(CACHE_FILE, MarkEditRequestDto.serializer(), mergeIncrementalDelta(
            cachedRows(), delta, { it.id ?: "entity:${it.entityId}" }, MarkEditRequestDto::isDeleted,
        ))
    }

    private fun ownerKey() =
        sessionManager.accountKey ?: SyncCheckpointDefaults.ownerKey("anonymous-local")

    private companion object { const val CACHE_FILE = "mark-edit-requests.json" }
}
