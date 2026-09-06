package com.mbd.cmscommon.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mbd.cmscommon.data.local.entity.AppLogEntity

@Dao
interface AppLogDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(record: AppLogEntity)

    @Query("SELECT * FROM app_logs ORDER BY occurredAtMillis ASC LIMIT :limit")
    suspend fun pendingBatch(limit: Int): List<AppLogEntity>

    @Query("DELETE FROM app_logs WHERE logId IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Query("SELECT COUNT(*) FROM app_logs")
    suspend fun countAll(): Int

    /** Evicts the oldest rows beyond [keep], so a device that can never reach Supabase does not grow unbounded. */
    @Query("DELETE FROM app_logs WHERE logId NOT IN (SELECT logId FROM app_logs ORDER BY occurredAtMillis DESC LIMIT :keep)")
    suspend fun trimOldest(keep: Int)
}
