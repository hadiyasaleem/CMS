package com.mbd.cmscommon.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mbd.cmscommon.data.local.entity.DocumentEntity

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents WHERE isDeleted = 0 ORDER BY updatedAt DESC")
    suspend fun getAll(): List<DocumentEntity>

    @Query("SELECT * FROM documents WHERE documentId = :id LIMIT 1")
    suspend fun getById(id: String): DocumentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<DocumentEntity>)

    @Query("DELETE FROM documents WHERE documentId = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM documents WHERE documentId IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    suspend fun applyDelta(upserts: List<DocumentEntity>, deletedIds: List<String>) {
        if (upserts.isNotEmpty()) upsertAll(upserts)
        if (deletedIds.isNotEmpty()) deleteByIds(deletedIds)
    }
}
