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

    /**
     * The same progressive rule the server's RLS applies: every level a notice targets (department, session,
     * shift) must match the reader. [includeAllScopes] skips the scope levels (admins, and teachers, whose
     * taught sessions and shifts are matched in Kotlin by NotificationAudienceContext).
     */
    @Query(
        """
        SELECT * FROM notifications
        WHERE isDeleted = 0
          AND (targetRole = :role OR targetRole = 'ALL')
          AND (:includeAllScopes = 1 OR targetOfferingId IS NULL OR targetOfferingId = :sessionId)
          AND (:includeAllScopes = 1 OR targetDeptId IS NULL OR targetDeptId = :departmentId)
          AND (:includeAllScopes = 1 OR targetShift IS NULL OR targetShift = :shift)
          AND (expiresAt IS NULL OR expiresAt >= :nowMillis)
        ORDER BY createdAt DESC
        """,
    )
    fun observeForRole(
        role: String,
        sessionId: String?,
        departmentId: String?,
        shift: String?,
        includeAllScopes: Boolean,
        nowMillis: Long,
    ): Flow<List<NotificationEntity>>

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
