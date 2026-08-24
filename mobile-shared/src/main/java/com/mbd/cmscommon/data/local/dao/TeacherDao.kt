package com.mbd.cmscommon.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mbd.cmscommon.data.local.entity.TeacherEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TeacherDao {
    @Query("SELECT * FROM teachers WHERE teacherId = :teacherId LIMIT 1")
    fun observe(teacherId: String): Flow<TeacherEntity>

    @Query("SELECT * FROM teachers WHERE isActive = 1 AND isDeleted = 0 ORDER BY name")
    fun observeActive(): Flow<List<TeacherEntity>>

    @Query("SELECT * FROM teachers WHERE teacherId = :id LIMIT 1")
    suspend fun getById(id: String): TeacherEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(teacher: TeacherEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<TeacherEntity>)

    @Query("DELETE FROM teachers WHERE teacherId = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM teachers WHERE teacherId IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Query("DELETE FROM teachers WHERE teacherId NOT IN (:ids)")
    suspend fun deleteNotIn(ids: List<String>)

    @Query("DELETE FROM teachers")
    suspend fun deleteAll()

    @Query("UPDATE teachers SET deptId = NULL WHERE deptId = :deptId")
    suspend fun clearDeptReference(deptId: String)

    suspend fun applyDelta(upserts: List<TeacherEntity>, deletedIds: List<String>) {
        if (upserts.isNotEmpty()) upsertAll(upserts)
        if (deletedIds.isNotEmpty()) deleteByIds(deletedIds)
    }
}
