package com.mbd.cmscommon.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mbd.cmscommon.data.local.entity.RoomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomDao {
    @Query("SELECT * FROM rooms WHERE isDeleted = 0 ORDER BY roomNo")
    fun observeActive(): Flow<List<RoomEntity>>

    @Query("SELECT * FROM rooms WHERE roomId = :id LIMIT 1")
    suspend fun getById(id: String): RoomEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(room: RoomEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<RoomEntity>)

    @Query("DELETE FROM rooms WHERE roomId = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM rooms WHERE roomId IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    suspend fun applyDelta(upserts: List<RoomEntity>, deletedIds: List<String>) {
        if (upserts.isNotEmpty()) upsertAll(upserts)
        if (deletedIds.isNotEmpty()) deleteByIds(deletedIds)
    }
}
