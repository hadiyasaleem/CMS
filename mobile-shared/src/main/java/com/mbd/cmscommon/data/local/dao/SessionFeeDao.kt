package com.mbd.cmscommon.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mbd.cmscommon.data.local.entity.SessionFeeEntity
import com.mbd.cmscommon.data.local.entity.SessionFeeHeadEntity

@Dao
interface SessionFeeDao {
    @Query("SELECT * FROM session_fees WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getFee(sessionId: String): SessionFeeEntity?

    @Query("SELECT * FROM session_fee_heads WHERE sessionId = :sessionId ORDER BY position")
    suspend fun getHeads(sessionId: String): List<SessionFeeHeadEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFees(items: List<SessionFeeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHeads(items: List<SessionFeeHeadEntity>)

    @Query("DELETE FROM session_fees WHERE sessionId IN (:ids)")
    suspend fun deleteFeesByIds(ids: List<String>)

    @Query("DELETE FROM session_fee_heads WHERE id IN (:ids)")
    suspend fun deleteHeadsByIds(ids: List<String>)

    @Query("DELETE FROM session_fee_heads WHERE sessionId = :sessionId")
    suspend fun deleteHeadsForSession(sessionId: String)

    suspend fun applyFeeDelta(upserts: List<SessionFeeEntity>, deletedIds: List<String>) {
        if (upserts.isNotEmpty()) upsertFees(upserts)
        if (deletedIds.isNotEmpty()) deleteFeesByIds(deletedIds)
    }

    suspend fun applyHeadDelta(upserts: List<SessionFeeHeadEntity>, deletedIds: List<String>) {
        if (upserts.isNotEmpty()) upsertHeads(upserts)
        if (deletedIds.isNotEmpty()) deleteHeadsByIds(deletedIds)
    }
}
