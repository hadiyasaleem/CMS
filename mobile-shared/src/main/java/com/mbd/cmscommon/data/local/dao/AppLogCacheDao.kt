package com.mbd.cmscommon.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mbd.cmscommon.data.local.entity.AppLogCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppLogCacheDao {
    @Query("SELECT * FROM app_log_cache ORDER BY occurredAtMillis DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<AppLogCacheEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<AppLogCacheEntity>)

    /** Evicts rows beyond [keep] (oldest first), so the viewer cache does not grow unbounded on-device. */
    @Query("DELETE FROM app_log_cache WHERE logId NOT IN (SELECT logId FROM app_log_cache ORDER BY occurredAtMillis DESC LIMIT :keep)")
    suspend fun trimOldest(keep: Int)
}
