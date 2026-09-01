package com.mbd.cmscommon.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mbd.cmscommon.data.local.entity.CalendarEventEntity
import com.mbd.cmscommon.data.local.entity.FineEntity
import com.mbd.cmscommon.data.local.entity.MarkEditRequestEntity

@Dao
interface CalendarEventDao {
    @Query("SELECT * FROM calendar_events WHERE isDeleted = 0 ORDER BY startDate")
    suspend fun getAll(): List<CalendarEventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<CalendarEventEntity>)

    @Query("DELETE FROM calendar_events WHERE eventId = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM calendar_events WHERE eventId IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    suspend fun applyDelta(upserts: List<CalendarEventEntity>, deletedIds: List<String>) {
        if (upserts.isNotEmpty()) upsertAll(upserts)
        if (deletedIds.isNotEmpty()) deleteByIds(deletedIds)
    }
}

@Dao
interface FineDao {
    @Query("SELECT * FROM fines WHERE sessionId = :sessionId AND rollNumber = :rollNumber AND isDeleted = 0 ORDER BY issuedAt DESC")
    suspend fun getForStudent(sessionId: String, rollNumber: String): List<FineEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<FineEntity>)

    @Query("DELETE FROM fines WHERE fineId = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM fines WHERE fineId IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    suspend fun applyDelta(upserts: List<FineEntity>, deletedIds: List<String>) {
        if (upserts.isNotEmpty()) upsertAll(upserts)
        if (deletedIds.isNotEmpty()) deleteByIds(deletedIds)
    }
}

@Dao
interface MarkEditRequestDao {
    @Query("SELECT * FROM mark_edit_requests WHERE requestId = :id LIMIT 1")
    suspend fun getById(id: String): MarkEditRequestEntity?

    @Query(
        "SELECT * FROM mark_edit_requests WHERE sessionId = :sessionId AND courseCode = :courseCode AND examType = :examType AND status = 'PENDING' AND isDeleted = 0",
    )
    suspend fun getPendingForAssignment(sessionId: String, courseCode: String, examType: String): List<MarkEditRequestEntity>

    @Query("SELECT * FROM mark_edit_requests WHERE status = 'PENDING' AND isDeleted = 0 ORDER BY requestedAt")
    suspend fun getPendingRequests(): List<MarkEditRequestEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<MarkEditRequestEntity>)

    @Query("DELETE FROM mark_edit_requests WHERE requestId = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM mark_edit_requests WHERE requestId IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    suspend fun applyDelta(upserts: List<MarkEditRequestEntity>, deletedIds: List<String>) {
        if (upserts.isNotEmpty()) upsertAll(upserts)
        if (deletedIds.isNotEmpty()) deleteByIds(deletedIds)
    }
}
