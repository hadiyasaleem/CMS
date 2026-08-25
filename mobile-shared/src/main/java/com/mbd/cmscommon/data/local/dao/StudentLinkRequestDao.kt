package com.mbd.cmscommon.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mbd.cmscommon.data.local.entity.StudentLinkRequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentLinkRequestDao {
    @Query("SELECT * FROM student_link_requests WHERE requestedByUid = :uid ORDER BY createdAt DESC")
    fun observeForRequester(uid: String): Flow<List<StudentLinkRequestEntity>>

    @Query("SELECT * FROM student_link_requests WHERE status = 'PENDING' AND isDeleted = 0 ORDER BY createdAt")
    fun observePending(): Flow<List<StudentLinkRequestEntity>>

    @Query("SELECT * FROM student_link_requests WHERE requestId = :id LIMIT 1")
    suspend fun getById(id: String): StudentLinkRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(request: StudentLinkRequestEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<StudentLinkRequestEntity>)

    @Query("DELETE FROM student_link_requests WHERE requestId = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM student_link_requests WHERE requestId IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    suspend fun applyDelta(upserts: List<StudentLinkRequestEntity>, deletedIds: List<String>) {
        if (upserts.isNotEmpty()) upsertAll(upserts)
        if (deletedIds.isNotEmpty()) deleteByIds(deletedIds)
    }
}
