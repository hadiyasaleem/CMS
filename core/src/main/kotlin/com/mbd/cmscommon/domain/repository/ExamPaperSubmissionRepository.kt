package com.mbd.cmscommon.domain.repository

import com.mbd.cmscommon.domain.model.ExamPaperSubmission
import java.io.File
import kotlinx.coroutines.flow.Flow

interface ExamPaperSubmissionRepository {
    /** The active (non-deleted) submission for one datesheet slot, if any -- at most one by the
     * table's own unique index. */
    fun observeSubmissionForSlot(datesheetSlotId: String): Flow<ExamPaperSubmission?>

    /** Every active submission, across every session/teacher -- backs the admin browse screen. */
    fun observeAllSubmissions(): Flow<List<ExamPaperSubmission>>

    /** Upserts the paper for [datesheetSlotId]: replaces the file in place (same storage object,
     * `upsert = true`) and updates the existing row if one is already active for this slot, or
     * inserts a new one otherwise -- so a reupload never leaves an orphaned object or row behind. */
    suspend fun uploadSubmission(
        datesheetSlotId: String,
        sessionId: String,
        semester: Int,
        courseCode: String,
        teacherId: String,
        fileBytes: ByteArray,
        fileName: String,
        description: String?,
    )

    /** Returns a local copy, serving a previously-cached download instead of re-fetching from
     * Storage when one already exists on disk. */
    suspend fun downloadTo(submission: ExamPaperSubmission, targetDir: File): File
    suspend fun deleteSubmission(id: String)

    /** One delta query across every session's submissions instead of one per session -- RLS already
     * restricts the rows a non-admin caller gets back, so this is a strict improvement for every role. */
    suspend fun syncAll()
}
