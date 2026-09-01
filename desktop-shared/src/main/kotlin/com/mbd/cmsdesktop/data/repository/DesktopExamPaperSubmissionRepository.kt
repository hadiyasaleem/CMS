package com.mbd.cmsdesktop.data.repository

import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.data.mapper.DesktopExamPaperSubmissionMapper
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.ExamPaperSubmissionDto
import com.mbd.cmscommon.data.sync.SyncCheckpointDefaults
import com.mbd.cmscommon.data.sync.fetchIncrementalDelta
import com.mbd.cmscommon.data.sync.mergeIncrementalDelta
import com.mbd.cmscommon.domain.model.ExamPaperSubmission
import com.mbd.cmscommon.domain.model.ExamType
import com.mbd.cmscommon.domain.model.examPaperUploadError
import com.mbd.cmscommon.domain.repository.ExamPaperSubmissionRepository
import com.mbd.cmsdesktop.data.cache.DesktopBootstrapSnapshotStore
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.Storage
import java.io.File
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/** Durable cache-first exam-paper submission repository. */
@Singleton
class DesktopExamPaperSubmissionRepository @Inject constructor(
    private val postgrest: Postgrest,
    private val storage: Storage,
    private val store: DesktopBootstrapSnapshotStore,
    private val sessionManager: SessionManager,
) : ExamPaperSubmissionRepository {

    private val cache = MutableStateFlow(rows().filterNot { it.isDeleted }.map(DesktopExamPaperSubmissionMapper::dtoToDomain))

    override fun observeSubmissionsForOffering(offeringId: String, subjectId: String): Flow<List<ExamPaperSubmission>> =
        cache.asStateFlow().map { list -> list.filter { it.offeringId == offeringId && it.subjectId == subjectId } }

    override suspend fun uploadSubmission(
        offeringId: String,
        subjectId: String,
        examType: ExamType,
        teacherId: String,
        fileBytes: ByteArray,
        fileName: String,
    ) {
        examPaperUploadError(fileName, fileBytes)?.let { throw IllegalArgumentException(it) }

        val ext = fileName.substringAfterLast('.', "").lowercase(Locale.ROOT)
        val storagePath = "$offeringId/$subjectId/${examType.name.lowercase(Locale.ROOT)}/${teacherId}_${System.currentTimeMillis()}.$ext"
        storage.from(SupabaseTables.BUCKET_EXAM_PAPERS).upload(storagePath, fileBytes) { upsert = true }

        val dto = ExamPaperSubmissionDto(
            sessionId = offeringId,
            courseCode = subjectId,
            examType = examType.name,
            teacherEmail = teacherId,
            storagePath = storagePath,
            fileName = fileName,
            fileSizeBytes = fileBytes.size.toLong(),
            createdBy = teacherId,
        )
        val inserted = postgrest.from(SupabaseTables.EXAM_PAPER_SUBMISSIONS)
            .insert(dto) { select() }
            .decodeList<ExamPaperSubmissionDto>()
        writeMerged(inserted)
    }

    override suspend fun downloadTo(submission: ExamPaperSubmission, targetDir: File): File {
        val bytes = storage.from(SupabaseTables.BUCKET_EXAM_PAPERS).downloadAuthenticated(submission.storagePath)
        val localFile = File(targetDir, submission.fileName)
        localFile.writeBytes(bytes)
        return localFile
    }

    override suspend fun deleteSubmission(id: String) {
        val existing = cache.value.find { it.submissionId == id }
        postgrest.from(SupabaseTables.EXAM_PAPER_SUBMISSIONS).update({ set("is_deleted", true) }) {
            filter { eq("id", id) }
        }
        if (existing != null && existing.storagePath.isNotBlank()) {
            runCatching { storage.from(SupabaseTables.BUCKET_EXAM_PAPERS).delete(existing.storagePath) }
        }
        writeRows(rows().filterNot { keyOf(it) == id })
    }

    override suspend fun sync(offeringId: String, subjectId: String) {
        val delta = fetchIncrementalDelta(
            store,
            ownerKey(),
            SupabaseTables.EXAM_PAPER_SUBMISSIONS,
            SyncCheckpointDefaults.scoped("session" to offeringId, "course" to subjectId),
            ExamPaperSubmissionDto::updatedAt,
        ) { since, from, to ->
            postgrest.from(SupabaseTables.EXAM_PAPER_SUBMISSIONS).select {
                filter {
                    eq("session_id", offeringId)
                    eq("course_code", subjectId)
                    gte("updated_at", since)
                }
                order("updated_at", Order.ASCENDING)
                range(from, to)
            }.decodeList()
        }
        writeMerged(delta)
    }

    private fun rows() = store.readRows(CACHE_FILE, ExamPaperSubmissionDto.serializer())

    private fun keyOf(dto: ExamPaperSubmissionDto) = dto.id ?: "entity:${dto.entityId}"

    private fun writeMerged(delta: List<ExamPaperSubmissionDto>) {
        writeRows(mergeIncrementalDelta(rows(), delta, ::keyOf, ExamPaperSubmissionDto::isDeleted))
    }

    private fun writeRows(updated: List<ExamPaperSubmissionDto>) {
        store.writeRows(CACHE_FILE, ExamPaperSubmissionDto.serializer(), updated)
        cache.value = updated.filterNot { it.isDeleted }.map(DesktopExamPaperSubmissionMapper::dtoToDomain)
    }

    private fun ownerKey() =
        sessionManager.accountKey ?: SyncCheckpointDefaults.ownerKey("anonymous-local")

    private companion object { const val CACHE_FILE = "exam-paper-submissions.json" }
}
