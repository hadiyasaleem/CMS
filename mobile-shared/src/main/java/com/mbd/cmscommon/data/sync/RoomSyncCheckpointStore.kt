package com.mbd.cmscommon.data.sync

import com.mbd.cmscommon.data.local.dao.TableSyncStateDao
import com.mbd.cmscommon.data.local.entity.TableSyncStateEntity
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

private fun TableSyncStateEntity.toDomain(): SyncCheckpoint =
    SyncCheckpoint(ownerKey, tableName, scopeKey, lastUpdatedAt, lastSuccessfulSyncAt)

private fun normalizeKey(value: String): String = value.trim().lowercase(Locale.ROOT)

private fun normalizeScope(value: String): String =
    value.ifBlank { SyncCheckpointDefaults.globalScope() }.trim().lowercase(Locale.ROOT)

@Singleton
class RoomSyncCheckpointStore @Inject constructor(
    private val dao: TableSyncStateDao,
) : SyncCheckpointStore {

    override suspend fun get(ownerKey: String, tableName: String, scopeKey: String): SyncCheckpoint? =
        dao.get(normalizeKey(ownerKey), normalizeKey(tableName), normalizeScope(scopeKey))?.toDomain()

    override suspend fun upsert(checkpoint: SyncCheckpoint) {
        val now = System.currentTimeMillis()
        val normalizedOwner = normalizeKey(checkpoint.ownerKey)
        val normalizedTable = normalizeKey(checkpoint.tableName)
        val normalizedScope = normalizeScope(checkpoint.scopeKey)
        val existing = dao.get(normalizedOwner, normalizedTable, normalizedScope)
        dao.upsert(
            TableSyncStateEntity(
                ownerKey = normalizedOwner,
                tableName = normalizedTable,
                scopeKey = normalizedScope,
                lastUpdatedAt = checkpoint.lastUpdatedAt,
                lastSuccessfulSyncAt = checkpoint.lastSuccessfulSyncAt,
                createdAt = existing?.createdAt ?: now,
                createdBy = existing?.createdBy ?: normalizedOwner,
                updatedAt = now,
                updatedBy = normalizedOwner,
            ),
        )
    }

    override suspend fun clear(ownerKey: String, tableName: String, scopeKey: String) {
        dao.clear(normalizeKey(ownerKey), normalizeKey(tableName), normalizeScope(scopeKey))
    }
}
