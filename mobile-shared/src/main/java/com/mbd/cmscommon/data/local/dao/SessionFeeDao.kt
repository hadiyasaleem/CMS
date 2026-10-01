package com.mbd.cmscommon.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mbd.cmscommon.data.local.entity.CollegeFeeEntity
import com.mbd.cmscommon.data.local.entity.CollegeFeeHeadEntity
import com.mbd.cmscommon.data.local.entity.SessionFeeEntity
import com.mbd.cmscommon.data.local.entity.SessionFeeHeadEntity

/** Fee structures are per (session, shift); heads belong to one shift's structure. */
@Dao
interface SessionFeeDao {
    @Query("SELECT * FROM session_fees WHERE sessionId = :sessionId AND shift = :shift LIMIT 1")
    suspend fun getFee(sessionId: String, shift: String): SessionFeeEntity?

    @Query("SELECT * FROM session_fees WHERE isDeleted = 0 ORDER BY sessionId, shift")
    suspend fun getAllFees(): List<SessionFeeEntity>

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

/** The college-wide base fee structure: one per shift, with its heads. */
@Dao
interface CollegeFeeDao {
    @Query("SELECT * FROM college_fees WHERE shift = :shift AND isDeleted = 0 LIMIT 1")
    suspend fun getFee(shift: String): CollegeFeeEntity?

    @Query("SELECT * FROM college_fees WHERE isDeleted = 0 ORDER BY shift")
    suspend fun getFees(): List<CollegeFeeEntity>

    @Query("SELECT * FROM college_fee_heads WHERE shift = :shift AND isDeleted = 0 ORDER BY position")
    suspend fun getHeads(shift: String): List<CollegeFeeHeadEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFees(items: List<CollegeFeeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHeads(items: List<CollegeFeeHeadEntity>)

    @Query("DELETE FROM college_fees WHERE shift = :shift")
    suspend fun deleteFee(shift: String)

    @Query("DELETE FROM college_fee_heads WHERE id IN (:ids)")
    suspend fun deleteHeadsByIds(ids: List<String>)

    @Query("DELETE FROM college_fee_heads WHERE shift = :shift")
    suspend fun deleteHeadsFor(shift: String)

    suspend fun applyFeeDelta(upserts: List<CollegeFeeEntity>, deletedShifts: List<String>) {
        if (upserts.isNotEmpty()) upsertFees(upserts)
        deletedShifts.forEach { deleteFee(it) }
    }

    suspend fun applyHeadDelta(upserts: List<CollegeFeeHeadEntity>, deletedIds: List<String>) {
        if (upserts.isNotEmpty()) upsertHeads(upserts)
        if (deletedIds.isNotEmpty()) deleteHeadsByIds(deletedIds)
    }
}
