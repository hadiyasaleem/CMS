package com.mbd.cmscommon.domain.repository

import com.mbd.cmscommon.domain.model.ExamPaperSubmission
import com.mbd.cmscommon.domain.model.ExamType
import java.io.File
import kotlinx.coroutines.flow.Flow

interface ExamPaperSubmissionRepository {
    fun observeSubmissionsForOffering(offeringId: String, subjectId: String): Flow<List<ExamPaperSubmission>>

    suspend fun uploadSubmission(offeringId: String, subjectId: String, examType: ExamType, teacherId: String, fileBytes: ByteArray, fileName: String)
    suspend fun downloadTo(submission: ExamPaperSubmission, targetDir: File): File
    suspend fun deleteSubmission(id: String)
    suspend fun sync(offeringId: String, subjectId: String)
}
