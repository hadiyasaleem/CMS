package com.mbd.cmscommon.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mbd.cmscommon.data.local.entity.SessionFeeEntity
import com.mbd.cmscommon.data.local.entity.SessionFeeHeadEntity

/** Fee structures are per (session, shift); heads belong to one shift's structure. */
@Dao
interface SessionFeeDao {
    @Query("SELECT * FROM session_fees WHERE sessionId = :sessionId AND shift = :shift LIMIT 1")
    suspend fun getFee(sessionId: String, shift: String): SessionFeeEntity?

    @Query("SELECT * FROM session_fees WHERE sessionId = :sessionId ORDER BY shift")
    suspend fun getFees(sessionId: String): List<SessionFeeEntity>

    @Query("SELECT * FROM session_fee_heads WHERE sessionId = :sessionId AND shift = :shift ORDER BY position")
    suspend fun getHeads(sessionId: String, shift: String): List<SessionFeeHeadEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFees(items: List<SessionFeeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHeads(items: List<SessionFeeHeadEntity>)

    @Query("DELETE FROM session_fees WHERE sessionId = :sessionId AND shift = :shift")
    suspend fun deleteFee(sessionId: String, shift: String)

    @Query("DELETE FROM session_fee_heads WHERE id IN (:ids)")
    suspend fun deleteHeadsByIds(ids: List<String>)

    @Query("DELETE FROM session_fee_heads WHERE sessionId = :sessionId AND shift = :shift")
    suspend fun deleteHeadsFor(sessionId: String, shift: String)

    /** [deleted] holds (sessionId, shift) keys of structures removed remotely. */
    suspend fun applyFeeDelta(upserts: List<SessionFeeEntity>, deleted: List<Pair<String, String>>) {
        if (upserts.isNotEmpty()) upsertFees(upserts)
        deleted.forEach { (sessionId, shift) -> deleteFee(sessionId, shift) }
    }

    suspend fun applyHeadDelta(upserts: List<SessionFeeHeadEntity>, deletedIds: List<String>) {
        if (upserts.isNotEmpty()) upsertHeads(upserts)
        if (deletedIds.isNotEmpty()) deleteHeadsByIds(deletedIds)
    }
}
