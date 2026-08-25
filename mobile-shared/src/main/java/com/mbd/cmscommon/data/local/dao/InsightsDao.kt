package com.mbd.cmscommon.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.mbd.cmscommon.data.local.entity.InsightAtRiskStudentEntity
import com.mbd.cmscommon.data.local.entity.InsightExamStatEntity
import com.mbd.cmscommon.data.local.entity.InsightSessionOverviewEntity

@Dao
interface InsightsDao {
    @Query("SELECT * FROM insight_session_overviews")
    suspend fun getSessionOverviews(): List<InsightSessionOverviewEntity>

    @Query("SELECT * FROM insight_at_risk_students")
    suspend fun getAtRiskStudents(): List<InsightAtRiskStudentEntity>

    @Query("SELECT * FROM insight_exam_stats")
    suspend fun getExamStats(): List<InsightExamStatEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSessionOverviews(items: List<InsightSessionOverviewEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAtRiskStudents(items: List<InsightAtRiskStudentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertExamStats(items: List<InsightExamStatEntity>)

    @Query("DELETE FROM insight_session_overviews")
    suspend fun clearSessionOverviews()

    @Query("DELETE FROM insight_at_risk_students")
    suspend fun clearAtRiskStudents()

    @Query("DELETE FROM insight_exam_stats")
    suspend fun clearExamStats()

    @Transaction
    suspend fun replaceSessionOverviews(items: List<InsightSessionOverviewEntity>) {
        clearSessionOverviews()
        if (items.isNotEmpty()) upsertSessionOverviews(items)
    }

    @Transaction
    suspend fun replaceAtRiskStudents(items: List<InsightAtRiskStudentEntity>) {
        clearAtRiskStudents()
        if (items.isNotEmpty()) upsertAtRiskStudents(items)
    }

    @Transaction
    suspend fun replaceExamStats(items: List<InsightExamStatEntity>) {
        clearExamStats()
        if (items.isNotEmpty()) upsertExamStats(items)
    }
}
