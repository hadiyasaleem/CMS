package com.mbd.cmscommon.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mbd.cmscommon.data.local.entity.BuildingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BuildingDao {
    @Query("SELECT * FROM buildings WHERE isDeleted = 0 ORDER BY name")
    fun observeActive(): Flow<List<BuildingEntity>>

    @Query("SELECT * FROM buildings WHERE buildingId = :id LIMIT 1")
    suspend fun getById(id: String): BuildingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(building: BuildingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<BuildingEntity>)

    @Query("DELETE FROM buildings WHERE buildingId = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM buildings WHERE buildingId IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    suspend fun applyDelta(upserts: List<BuildingEntity>, deletedIds: List<String>) {
        if (upserts.isNotEmpty()) upsertAll(upserts)
        if (deletedIds.isNotEmpty()) deleteByIds(deletedIds)
    }
}
