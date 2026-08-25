package com.mbd.cmsdesktop.data.repository

import com.mbd.cmscommon.data.mapper.DesktopExamPaperSubmissionMapper
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.ExamPaperSubmissionDto
import com.mbd.cmscommon.domain.model.ExamPaperSubmission
import com.mbd.cmscommon.domain.model.ExamType
import com.mbd.cmscommon.domain.model.examPaperUploadError
import com.mbd.cmscommon.domain.repository.ExamPaperSubmissionRepository
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

/**
 * No local persistence. [cache] is one flat list spanning every (offeringId, subjectId) pair ever
 * synced; [sync] filterNot-replaces just its own scope, mirroring the other session-scoped repos.
 */
@Singleton
class DesktopExamPaperSubmissionRepository @Inject constructor(
    private val postgrest: Postgrest,
    private val storage: Storage,
) : ExamPaperSubmissionRepository {

    private val cache = MutableStateFlow<List<ExamPaperSubmission>>(emptyList())

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
        postgrest.from(SupabaseTables.EXAM_PAPER_SUBMISSIONS).insert(dto)
        sync(offeringId, subjectId)
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
        if (existing != null) sync(existing.offeringId, existing.subjectId)
    }

    override suspend fun sync(offeringId: String, subjectId: String) {
        val rows = postgrest.from(SupabaseTables.EXAM_PAPER_SUBMISSIONS).select {
            filter {
                eq("session_id", offeringId)
                eq("course_code", subjectId)
                eq("is_deleted", false)
            }
            order("uploaded_at", Order.DESCENDING)
        }.decodeList<ExamPaperSubmissionDto>()

        val mapped = rows.map { DesktopExamPaperSubmissionMapper.dtoToDomain(it) }
        cache.value = cache.value.filterNot { it.offeringId == offeringId && it.subjectId == subjectId } + mapped
    }
}
