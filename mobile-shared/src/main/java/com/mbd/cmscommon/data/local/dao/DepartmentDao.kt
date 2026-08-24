package com.mbd.cmscommon.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mbd.cmscommon.data.local.entity.DepartmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DepartmentDao {
    @Query("SELECT * FROM departments WHERE isActive = 1 AND isDeleted = 0 ORDER BY name")
    fun observeActive(): Flow<List<DepartmentEntity>>

    @Query("SELECT * FROM departments WHERE deptId = :id LIMIT 1")
    suspend fun getById(id: String): DepartmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(department: DepartmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<DepartmentEntity>)

    @Delete
    suspend fun delete(department: DepartmentEntity)

    @Query("DELETE FROM departments WHERE deptId = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM departments WHERE deptId IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Query("DELETE FROM departments WHERE deptId NOT IN (:ids)")
    suspend fun deleteNotIn(ids: List<String>)

    @Query("DELETE FROM departments")
    suspend fun deleteAll()

    suspend fun applyDelta(upserts: List<DepartmentEntity>, deletedIds: List<String>) {
        if (upserts.isNotEmpty()) upsertAll(upserts)
        if (deletedIds.isNotEmpty()) deleteByIds(deletedIds)
    }
}
