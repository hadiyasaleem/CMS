package com.mbd.cmscommon.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mbd.cmscommon.data.local.entity.AdministratorAccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AdministratorAccountDao {
    @Query("SELECT * FROM administrator_accounts WHERE isDeleted = 0 ORDER BY email")
    fun observeAll(): Flow<List<AdministratorAccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<AdministratorAccountEntity>)

    @Query("DELETE FROM administrator_accounts")
    suspend fun clear()

    suspend fun applyDelta(rows: List<AdministratorAccountEntity>) {
        if (rows.isNotEmpty()) upsertAll(rows)
    }
}
