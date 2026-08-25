package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.data.local.dao.AcademicSessionDao
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
import com.mbd.cmscommon.domain.model.ExamType
import com.mbd.cmscommon.domain.model.examPaperUploadError
import com.mbd.cmscommon.domain.repository.ExamPaperSubmissionRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.Storage
import java.io.File
import java.time.Instant
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExamPaperSubmissionRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val storage: Storage,
    private val submissionDao: ExamPaperSubmissionDao,
    private val academicSessionDao: AcademicSessionDao,
    private val checkpointStore: SyncCheckpointStore,
    private val sessionManager: SessionManager,
) : ExamPaperSubmissionRepository {

    private fun syncOwnerKey(): String = sessionManager.accountKey ?: SyncCheckpointDefaults.ownerKey("anonymous-local")

    override fun observeSubmissionsForOffering(offeringId: String, subjectId: String): Flow<List<ExamPaperSubmission>> =
        submissionDao.observeForOffering(offeringId, subjectId).map { rows -> rows.map { ExamPaperSubmissionMapper.entityToDomain(it) } }

    override suspend fun uploadSubmission(offeringId: String, subjectId: String, examType: ExamType, teacherId: String, fileBytes: ByteArray, fileName: String) {
        examPaperUploadError(fileName, fileBytes)?.let { throw IllegalArgumentException(it) }

        val ext = fileName.substringAfterLast('.', "").lowercase(Locale.ROOT)
        val storagePath = "$offeringId/$subjectId/${examType.name.lowercase(Locale.ROOT)}/${teacherId}_${System.currentTimeMillis()}.$ext"
        storage.from(SupabaseTables.BUCKET_EXAM_PAPERS).upload(storagePath, fileBytes) { upsert = true }

        val semester = academicSessionDao.getById(offeringId)?.currentSemester ?: 1
        val dto = ExamPaperSubmissionDto(
            sessionId = offeringId,
            semester = semester,
            courseCode = subjectId,
            examType = examType.name,
            teacherEmail = teacherId,
            storagePath = storagePath,
            fileName = fileName,
            fileSizeBytes = fileBytes.size.toLong(),
            createdBy = teacherId,
        )
        val inserted = postgrest.from(SupabaseTables.EXAM_PAPER_SUBMISSIONS).insert(dto) { select() }.decodeList<ExamPaperSubmissionDto>().first()
        submissionDao.upsertAll(listOf(ExamPaperSubmissionMapper.dtoToEntity(inserted)))
    }

    override suspend fun downloadTo(submission: ExamPaperSubmission, targetDir: File): File {
        val bytes = storage.from(SupabaseTables.BUCKET_EXAM_PAPERS).downloadAuthenticated(submission.storagePath)
        val localFile = File(targetDir, submission.fileName)
        localFile.writeBytes(bytes)
        return localFile
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

    override suspend fun sync(offeringId: String, subjectId: String) {
        val ownerKey = syncOwnerKey()
        val scopeKey = SyncCheckpointDefaults.scoped("offering" to offeringId, "subject" to subjectId)
        val checkpoint = checkpointStore.get(ownerKey, SupabaseTables.EXAM_PAPER_SUBMISSIONS, scopeKey)
        val since = checkpoint?.lastUpdatedAt ?: SyncCheckpointDefaults.EPOCH
        var maxUpdatedAt = since

        var offset = 0L
        while (true) {
            val page = postgrest.from(SupabaseTables.EXAM_PAPER_SUBMISSIONS).select {
                filter {
                    eq("session_id", offeringId)
                    eq("course_code", subjectId)
                    gte("updated_at", since)
                }
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
