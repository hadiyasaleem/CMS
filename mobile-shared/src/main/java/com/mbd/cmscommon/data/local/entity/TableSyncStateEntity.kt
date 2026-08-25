package com.mbd.cmscommon.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "table_sync_state", primaryKeys = ["owner_key", "table_name", "scope_key"])
data class TableSyncStateEntity(
    @ColumnInfo(name = "owner_key") val ownerKey: String,
    @ColumnInfo(name = "table_name") val tableName: String,
    @ColumnInfo(name = "scope_key") val scopeKey: String,
    @ColumnInfo(name = "last_updated_at") val lastUpdatedAt: String,
    @ColumnInfo(name = "last_successful_sync_at") val lastSuccessfulSyncAt: String,
    @ColumnInfo(name = "created_at") val createdAt: Long = 0L,
    @ColumnInfo(name = "created_by") val createdBy: String? = null,
    @ColumnInfo(name = "updated_at") val updatedAt: Long = 0L,
    @ColumnInfo(name = "updated_by") val updatedBy: String? = null,
)
