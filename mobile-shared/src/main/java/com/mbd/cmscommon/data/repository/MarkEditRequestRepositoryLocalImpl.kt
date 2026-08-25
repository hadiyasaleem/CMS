package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.data.local.dao.MarkEditRequestDao
import com.mbd.cmscommon.data.mapper.MarkEditRequestEntityMapper
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.MarkEditRequestDto
import com.mbd.cmscommon.data.sync.SyncCheckpoint
import com.mbd.cmscommon.data.sync.SyncCheckpointDefaults
import com.mbd.cmscommon.data.sync.SyncCheckpointStore
import com.mbd.cmscommon.data.sync.maxRemoteUpdatedAt
import com.mbd.cmscommon.domain.model.ExamType
import com.mbd.cmscommon.domain.model.MarkEditRequest
import com.mbd.cmscommon.domain.repository.MarkEditRequestRepository
import com.mbd.cmscommon.data.remote.PgTime
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import java.time.Instant
import javax.inject.Inject

class MarkEditRequestRepositoryLocalImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val requestDao: MarkEditRequestDao,
    private val checkpointStore: SyncCheckpointStore,
    private val sessionManager: SessionManager,
) : MarkEditRequestRepository {

    override suspend fun getPendingForAssignment(sessionId: String, courseCode: String, examType: ExamType): List<MarkEditRequest> {
        val scopeKey = SyncCheckpointDefaults.scoped(
            "session_id" to sessionId,
            "course_code" to courseCode,
            "exam_type" to examType.name,
            "status" to MARK_EDIT_STATUS_PENDING,
        )
        runCatching {
            syncDelta(scopeKey) {
                filter {
                    eq("session_id", sessionId)
                    eq("course_code", courseCode)
                    eq("exam_type", examType.name)
                    eq("status", MARK_EDIT_STATUS_PENDING)
                }
            }
        }
        return requestDao.getPendingForAssignment(sessionId, courseCode, examType.name).map { MarkEditRequestEntityMapper.entityToDomain(it) }
    }

    override suspend fun getPendingRequests(): List<MarkEditRequest> {
        val scopeKey = SyncCheckpointDefaults.scoped("status" to MARK_EDIT_STATUS_PENDING)
        runCatching {
            syncDelta(scopeKey) {
                filter { eq("status", MARK_EDIT_STATUS_PENDING) }
            }
        }
        return requestDao.getPendingRequests().map { MarkEditRequestEntityMapper.entityToDomain(it) }
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
            status = MARK_EDIT_STATUS_PENDING,
            requestedBy = requestedBy,
        )
        val inserted = postgrest.from(SupabaseTables.MARK_EDIT_REQUESTS).insert(dto) { select() }.decodeList<MarkEditRequestDto>().first()
        requestDao.upsertAll(listOf(MarkEditRequestEntityMapper.dtoToEntity(inserted)))
    }

    override suspend fun approveRequest(requestId: String, reviewedBy: String) {
        val request = fetchRequest(requestId)
        postgrest.from(SupabaseTables.SESSION_MARKS).update({ set("score", request.requestedScore) }) {
            filter {
                eq("session_id", request.sessionId ?: "")
                eq("semester", request.semester)
                eq("course_code", request.courseCode ?: "")
                eq("exam_type", request.examType ?: "")
                eq("roll_number", request.rollNumber ?: "")
            }
        }
        updateStatus(requestId, "APPROVED", reviewedBy)
    }

    override suspend fun rejectRequest(requestId: String, reviewedBy: String) {
        updateStatus(requestId, "REJECTED", reviewedBy)
    }

    private suspend fun fetchRequest(requestId: String): MarkEditRequestDto =
        postgrest.from(SupabaseTables.MARK_EDIT_REQUESTS).select { filter { eq("id", requestId) } }
            .decodeList<MarkEditRequestDto>().first()

    private suspend fun updateStatus(requestId: String, status: String, reviewedBy: String) {
        postgrest.from(SupabaseTables.MARK_EDIT_REQUESTS).update({
            set("status", status)
            set("reviewed_by", reviewedBy)
            set("reviewed_at", Instant.now().toString())
        }) {
            filter { eq("id", requestId) }
        }
        requestDao.deleteById(requestId)
    }

    private suspend fun syncDelta(scopeKey: String, applyFilter: io.github.jan.supabase.postgrest.query.PostgrestRequestBuilder.() -> Unit) {
        val ownerKey = sessionManager.accountKey ?: SyncCheckpointDefaults.ownerKey("anonymous-local")
        val checkpoint = checkpointStore.get(ownerKey, SupabaseTables.MARK_EDIT_REQUESTS, scopeKey)
        val since = checkpoint?.lastUpdatedAt ?: SyncCheckpointDefaults.EPOCH
        var maxUpdatedAt = since

        var offset = 0L
        while (true) {
            val page = postgrest.from(SupabaseTables.MARK_EDIT_REQUESTS).select {
                applyFilter()
                filter { gte("updated_at", since) }
                order("updated_at", Order.ASCENDING)
                range(offset, offset + PAGE_SIZE - 1)
            }.decodeList<MarkEditRequestDto>()
            if (page.isEmpty()) break

            val entities = page.map { MarkEditRequestEntityMapper.dtoToEntity(it) }
            val (deleted, active) = entities.partition { it.isDeleted }
            requestDao.applyDelta(active, deleted.map { it.requestId })
            maxUpdatedAt = page.maxRemoteUpdatedAt(maxUpdatedAt) { it.updatedAt }

            if (page.size < PAGE_SIZE) break
            offset += PAGE_SIZE
        }

        checkpointStore.upsert(
            SyncCheckpoint(
                ownerKey,
                SupabaseTables.MARK_EDIT_REQUESTS,
                scopeKey,
                maxUpdatedAt,
                PgTime.format(Instant.now()) ?: since,
            ),
        )
    }

    private companion object {
        const val PAGE_SIZE = 500L
    }
}
