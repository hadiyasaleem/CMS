package com.mbd.cmscommon.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mbd.cmscommon.data.local.entity.SyncStateEntity

@Dao
interface SyncStateDao {
    @Query("SELECT lastSyncedAt FROM sync_state WHERE collectionName = :collectionName LIMIT 1")
    suspend fun getLastSyncedAt(collectionName: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: SyncStateEntity)
}
