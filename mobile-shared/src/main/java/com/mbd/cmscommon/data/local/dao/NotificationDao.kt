package com.mbd.cmscommon.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mbd.cmscommon.data.local.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE createdByUid = :uid ORDER BY createdAt DESC")
    fun observeAuthoredBy(uid: String): Flow<List<NotificationEntity>>

    @Query(
        """
        SELECT * FROM notifications
        WHERE isDeleted = 0
          AND (targetRole = :role OR targetRole = 'ALL')
          AND (:includeAllScopes = 1 OR targetOfferingId IS NULL OR targetOfferingId = :sessionId)
          AND (:includeAllScopes = 1 OR targetDeptId IS NULL OR targetDeptId = :departmentId)
          AND (expiresAt IS NULL OR expiresAt >= :nowMillis)
        ORDER BY createdAt DESC
        """,
    )
    fun observeForRole(
        role: String,
        sessionId: String?,
        departmentId: String?,
        includeAllScopes: Boolean,
        nowMillis: Long,
    ): Flow<List<NotificationEntity>>

    @Query(
        """
        SELECT COUNT(*) FROM notifications
        WHERE isDeleted = 0
          AND (targetRole = :role OR targetRole = 'ALL')
          AND (:includeAllScopes = 1 OR targetOfferingId IS NULL OR targetOfferingId = :sessionId)
          AND (:includeAllScopes = 1 OR targetDeptId IS NULL OR targetDeptId = :departmentId)
          AND (expiresAt IS NULL OR expiresAt >= :nowMillis)
          AND createdAt >= :sinceMillis
        """,
    )
    fun observeUnreadCount(
        role: String,
        sessionId: String?,
        departmentId: String?,
        includeAllScopes: Boolean,
        nowMillis: Long,
        sinceMillis: Long,
    ): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<NotificationEntity>)

    @Query("DELETE FROM notifications WHERE notificationId = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM notifications WHERE notificationId IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    suspend fun applyDelta(upserts: List<NotificationEntity>, deletedIds: List<String>) {
        if (upserts.isNotEmpty()) upsertAll(upserts)
        if (deletedIds.isNotEmpty()) deleteByIds(deletedIds)
    }
}
