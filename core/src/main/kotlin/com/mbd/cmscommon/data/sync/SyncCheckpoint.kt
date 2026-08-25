package com.mbd.cmscommon.data.sync

data class SyncCheckpoint(
    val ownerKey: String,
    val tableName: String,
    val scopeKey: String,
    val lastUpdatedAt: String,
    val lastSuccessfulSyncAt: String,
)

object SyncCheckpointDefaults {
    const val EPOCH = "1970-01-01T00:00:00Z"

    fun ownerKey(email: String): String = email.trim().lowercase()

    fun globalScope(): String = "global"

    fun scoped(vararg parts: Pair<String, Any?>): String {
        val joined = parts
            .filter { (_, value) -> value != null && value.toString().isNotBlank() }
            .joinToString("|") { (key, value) -> "${key.trim().lowercase()}=${value.toString().trim().lowercase()}" }
        return joined.ifBlank { globalScope() }
    }
}

interface SyncCheckpointStore {
    suspend fun get(ownerKey: String, tableName: String, scopeKey: String): SyncCheckpoint?
    suspend fun upsert(checkpoint: SyncCheckpoint)
    suspend fun clear(ownerKey: String, tableName: String, scopeKey: String)
}
