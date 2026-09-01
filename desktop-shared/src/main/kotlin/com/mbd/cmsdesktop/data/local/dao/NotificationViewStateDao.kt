package com.mbd.cmsdesktop.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mbd.cmsdesktop.data.local.entity.NotificationViewStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationViewStateDao {
    @Query("SELECT lastViewedAt FROM notification_view_state WHERE id = 'default' LIMIT 1")
    fun observeLastViewedAt(): Flow<Long?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: NotificationViewStateEntity)
}
