package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.data.local.dao.ExamPaperSubmissionDao
import com.mbd.cmscommon.data.mapper.ExamPaperSubmissionMapper
import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.ExamPaperSubmissionDto
import com.mbd.cmscommon.data.sync.SyncCheckpoint
import com.mbd.cmscommon.data.sync.SyncCheckpointDefaults
import com.mbd.cmscommon.data.sync.SyncCheckpointStore
import com.mbd.cmscommon.data.sync.maxRemoteUpdatedAt
import com.mbd.cmscommon.domain.model.ExamPaperSubmission
import com.mbd.cmscommon.domain.model.examPaperUploadError
import com.mbd.cmscommon.domain.repository.ExamPaperSubmissionRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.Storage
import java.io.File
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExamPaperSubmissionRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val storage: Storage,
    private val submissionDao: ExamPaperSubmissionDao,
    private val checkpointStore: SyncCheckpointStore,
    private val sessionManager: SessionManager,
) : ExamPaperSubmissionRepository {

    private fun syncOwnerKey(): String = sessionManager.accountKey ?: SyncCheckpointDefaults.ownerKey("anonymous-local")

    override fun observeSubmissionForSlot(datesheetSlotId: String): Flow<ExamPaperSubmission?> =
        submissionDao.observeForSlot(datesheetSlotId).map { it?.let { ExamPaperSubmissionMapper.entityToDomain(it) } }

    override fun observeAllSubmissions(): Flow<List<ExamPaperSubmission>> =
        submissionDao.observeAll().map { rows -> rows.map { ExamPaperSubmissionMapper.entityToDomain(it) } }

    override suspend fun uploadSubmission(
        datesheetSlotId: String,
        sessionId: String,
        semester: Int,
        courseCode: String,
        teacherId: String,
        fileBytes: ByteArray,
        fileName: String,
        description: String?,
    ) {
        examPaperUploadError(fileName, fileBytes)?.let { throw IllegalArgumentException(it) }

        // Path is keyed by the slot itself (not a timestamp), and always .pdf (the bucket only
        // allows application/pdf) -- so a reupload always targets the same object and `upsert = true`
        // replaces it in place. No cleanup of a stale object is ever needed.
        val storagePath = "$sessionId/$semester/$courseCode/$datesheetSlotId.pdf"
        storage.from(SupabaseTables.BUCKET_EXAM_PAPERS).upload(storagePath, fileBytes) { upsert = true }

        val existing = postgrest.from(SupabaseTables.EXAM_PAPER_SUBMISSIONS).select {
            filter {
                eq("datesheet_slot_id", datesheetSlotId)
                eq("is_deleted", false)
            }
        }.decodeList<ExamPaperSubmissionDto>().firstOrNull()

        val existingId = existing?.id
        val saved = if (existing != null && existingId != null) {
            val nowIso = PgTime.format(Instant.now())
            postgrest.from(SupabaseTables.EXAM_PAPER_SUBMISSIONS).update({
                set("file_name", fileName)
                set("file_size_bytes", fileBytes.size.toLong())
                set("description", description)
                set("uploaded_at", nowIso)
            }) {
                filter { eq("id", existingId) }
            }
            existing.copy(fileName = fileName, fileSizeBytes = fileBytes.size.toLong(), description = description, uploadedAt = nowIso)
        } else {
            val dto = ExamPaperSubmissionDto(
                datesheetSlotId = datesheetSlotId,
                sessionId = sessionId,
                semester = semester,
                courseCode = courseCode,
                teacherEmail = teacherId,
                storagePath = storagePath,
                fileName = fileName,
                fileSizeBytes = fileBytes.size.toLong(),
                description = description,
                createdBy = teacherId,
            )
            postgrest.from(SupabaseTables.EXAM_PAPER_SUBMISSIONS).insert(dto) { select() }.decodeList<ExamPaperSubmissionDto>().first()
        }
        submissionDao.upsertAll(listOf(ExamPaperSubmissionMapper.dtoToEntity(saved)))
    }

    override suspend fun downloadTo(submission: ExamPaperSubmission, targetDir: File): File {
        val cacheDir = File(targetDir, "exam_papers").apply { mkdirs() }
        val cacheFile = File(cacheDir, "${submission.submissionId}_${submission.fileName}")
        if (cacheFile.exists()) return cacheFile
        val bytes = storage.from(SupabaseTables.BUCKET_EXAM_PAPERS).downloadAuthenticated(submission.storagePath)
        cacheFile.writeBytes(bytes)
        return cacheFile
    }

    override suspend fun deleteSubmission(id: String) {
        val existing = submissionDao.getById(id)
        val path = existing?.storagePath
        postgrest.from(SupabaseTables.EXAM_PAPER_SUBMISSIONS).update({ set("is_deleted", true) }) {
            filter { eq("id", id) }
        }
        if (!path.isNullOrBlank()) {
            runCatching { storage.from(SupabaseTables.BUCKET_EXAM_PAPERS).delete(path) }
        }
        submissionDao.deleteById(id)
    }

    override suspend fun syncAll() {
        val ownerKey = syncOwnerKey()
        val scopeKey = SyncCheckpointDefaults.globalScope()
        val checkpoint = checkpointStore.get(ownerKey, SupabaseTables.EXAM_PAPER_SUBMISSIONS, scopeKey)
        val since = checkpoint?.lastUpdatedAt ?: SyncCheckpointDefaults.EPOCH
        var maxUpdatedAt = since

        var offset = 0L
        while (true) {
            val page = postgrest.from(SupabaseTables.EXAM_PAPER_SUBMISSIONS).select {
                filter { gte("updated_at", since) }
                order("updated_at", Order.ASCENDING)
                range(offset, offset + PAGE_SIZE - 1)
            }.decodeList<ExamPaperSubmissionDto>()
            if (page.isEmpty()) break

            val entities = page.map { ExamPaperSubmissionMapper.dtoToEntity(it) }
            val (deleted, active) = entities.partition { it.isDeleted }
            submissionDao.applyDelta(active, deleted.map { it.submissionId })
            maxUpdatedAt = page.maxRemoteUpdatedAt(maxUpdatedAt) { it.updatedAt }

            if (page.size < PAGE_SIZE) break
            offset += PAGE_SIZE
        }

        checkpointStore.upsert(SyncCheckpoint(ownerKey, SupabaseTables.EXAM_PAPER_SUBMISSIONS, scopeKey, maxUpdatedAt, PgTime.format(Instant.now()) ?: since))
    }

    private companion object {
        const val PAGE_SIZE = 500L
    }
}
