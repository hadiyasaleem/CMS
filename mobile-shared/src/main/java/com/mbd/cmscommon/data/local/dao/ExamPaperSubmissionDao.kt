package com.mbd.cmscommon.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mbd.cmscommon.data.local.entity.ExamPaperSubmissionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamPaperSubmissionDao {
    @Query("SELECT * FROM exam_paper_submissions WHERE offeringId = :offeringId AND subjectId = :subjectId AND isDeleted = 0 ORDER BY uploadedAt DESC")
    fun observeForOffering(offeringId: String, subjectId: String): Flow<List<ExamPaperSubmissionEntity>>

    @Query("SELECT * FROM exam_paper_submissions WHERE submissionId = :id LIMIT 1")
    suspend fun getById(id: String): ExamPaperSubmissionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ExamPaperSubmissionEntity>)

    @Query("DELETE FROM exam_paper_submissions WHERE submissionId = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM exam_paper_submissions WHERE submissionId IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Query("DELETE FROM exam_paper_submissions WHERE offeringId = :offeringId")
    suspend fun deleteAllForOffering(offeringId: String)

    @Query("DELETE FROM exam_paper_submissions WHERE subjectId = :subjectId")
    suspend fun deleteAllForSubject(subjectId: String)

    suspend fun applyDelta(upserts: List<ExamPaperSubmissionEntity>, deletedIds: List<String>) {
        if (upserts.isNotEmpty()) upsertAll(upserts)
        if (deletedIds.isNotEmpty()) deleteByIds(deletedIds)
    }
}
