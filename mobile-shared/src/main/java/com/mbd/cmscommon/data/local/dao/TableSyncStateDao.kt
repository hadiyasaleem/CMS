package com.mbd.cmscommon.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mbd.cmscommon.data.local.entity.TableSyncStateEntity

@Dao
interface TableSyncStateDao {
    @Query("SELECT * FROM table_sync_state WHERE owner_key = :ownerKey AND table_name = :tableName AND scope_key = :scopeKey LIMIT 1")
    suspend fun get(ownerKey: String, tableName: String, scopeKey: String): TableSyncStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: TableSyncStateEntity)

    @Query("DELETE FROM table_sync_state WHERE owner_key = :ownerKey AND table_name = :tableName AND scope_key = :scopeKey")
    suspend fun clear(ownerKey: String, tableName: String, scopeKey: String)
}
