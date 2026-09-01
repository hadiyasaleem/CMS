package com.mbd.cmsdesktop.data.cache

import com.mbd.cmscommon.data.remote.dto.AcademicSessionDto
import com.mbd.cmscommon.data.remote.dto.DepartmentDto
import com.mbd.cmscommon.data.remote.dto.SessionStudentDto
import com.mbd.cmscommon.data.remote.dto.TeacherDto
import com.mbd.cmscommon.data.sync.SyncCheckpoint
import com.mbd.cmscommon.data.sync.SyncCheckpointDefaults
import com.mbd.cmscommon.data.sync.SyncCheckpointStore
import com.mbd.cmscommon.domain.model.UserRole
import java.io.File
import java.nio.file.StandardCopyOption
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/** Desktop's durable JSON cache and per-table incremental-sync checkpoint store. */
class DesktopBootstrapSnapshotStore(baseDir: File = defaultBaseDir()) : SyncCheckpointStore {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    private val cacheDir: File = File(baseDir, "cache").apply { mkdirs() }
    private val checkpointLock = Any()
    private val rowLocks = ConcurrentHashMap<String, Any>()

    init {
        migrateLegacySharedCache(baseDir, cacheDir)
    }

    fun readDepartments(): List<DepartmentDto> = readRows("departments.json", DepartmentDto.serializer())
    fun writeDepartments(rows: List<DepartmentDto>) = writeRows("departments.json", DepartmentDto.serializer(), rows)

    fun readTeachers(): List<TeacherDto> = readRows("teachers.json", TeacherDto.serializer())
    fun writeTeachers(rows: List<TeacherDto>) = writeRows("teachers.json", TeacherDto.serializer(), rows)

    fun readSessions(): List<AcademicSessionDto> = readRows("sessions.json", AcademicSessionDto.serializer())
    fun writeSessions(rows: List<AcademicSessionDto>) = writeRows("sessions.json", AcademicSessionDto.serializer(), rows)

    fun readStudents(): List<SessionStudentDto> = readRows("students.json", SessionStudentDto.serializer())
    fun writeStudents(rows: List<SessionStudentDto>) = writeRows("students.json", SessionStudentDto.serializer(), rows)

    fun readRole(): UserRole? {
        val file = cacheFile("role.json")
        if (!file.exists()) return null
        return runCatching { json.decodeFromString(RoleSnapshot.serializer(), file.readText()).toUserRole() }.getOrNull()
    }

    fun writeRole(role: UserRole?) {
        val file = cacheFile("role.json")
        if (role == null) {
            runCatching { file.delete() }
            return
        }
        writeTextSafely(file, json.encodeToString(RoleSnapshot.serializer(), RoleSnapshot.from(role)))
    }

    fun isBootstrapComplete(scope: String, accountKey: String): Boolean =
        readBootstrapState().completedKeys.contains(bootstrapKey(scope, accountKey))

    fun markBootstrapComplete(scope: String, accountKey: String) {
        val current = readBootstrapState()
        writeBootstrapState(current.copy(completedKeys = current.completedKeys + bootstrapKey(scope, accountKey)))
    }

    fun <T> readRows(fileName: String, serializer: KSerializer<T>): List<T> {
        val file = cacheFile(fileName)
        if (!file.exists()) return emptyList()
        return runCatching {
            json.decodeFromString(ListSerializer(serializer), file.readText())
        }.getOrDefault(emptyList())
    }

    fun <T> writeRows(fileName: String, serializer: KSerializer<T>, rows: List<T>) =
        synchronized(rowLocks.computeIfAbsent(fileName) { Any() }) {
            writeRowsLocked(fileName, serializer, rows)
        }

    /** Atomically reads, transforms, and persists one snapshot file. */
    fun <T> updateRows(
        fileName: String,
        serializer: KSerializer<T>,
        transform: (List<T>) -> List<T>,
    ): List<T> = synchronized(rowLocks.computeIfAbsent(fileName) { Any() }) {
        val updated = transform(readRowsLocked(fileName, serializer))
        writeRowsLocked(fileName, serializer, updated)
        updated
    }

    private fun <T> readRowsLocked(fileName: String, serializer: KSerializer<T>): List<T> {
        val file = cacheFile(fileName)
        if (!file.exists()) return emptyList()
        return runCatching { json.decodeFromString(ListSerializer(serializer), file.readText()) }.getOrDefault(emptyList())
    }

    private fun <T> writeRowsLocked(fileName: String, serializer: KSerializer<T>, rows: List<T>) {
        writeTextSafely(cacheFile(fileName), json.encodeToString(ListSerializer(serializer), rows))
    }

    override suspend fun get(ownerKey: String, tableName: String, scopeKey: String): SyncCheckpoint? = synchronized(checkpointLock) {
        val key = checkpointKey(ownerKey, tableName, scopeKey)
        readCheckpointState().entries.firstOrNull { it.key == key }?.toDomain()
    }

    override suspend fun upsert(checkpoint: SyncCheckpoint) = synchronized(checkpointLock) {
        val normalized = checkpoint.normalized()
        val key = checkpointKey(normalized.ownerKey, normalized.tableName, normalized.scopeKey)
        val current = readCheckpointState()
        writeCheckpointState(current.copy(entries = current.entries.filterNot { it.key == key } + DesktopSyncCheckpointSnapshot.from(normalized)))
    }

    override suspend fun clear(ownerKey: String, tableName: String, scopeKey: String) = synchronized(checkpointLock) {
        val key = checkpointKey(ownerKey, tableName, scopeKey)
        val current = readCheckpointState()
        writeCheckpointState(current.copy(entries = current.entries.filterNot { it.key == key }))
    }

    private fun readBootstrapState(): BootstrapStateSnapshot {
        val file = cacheFile("bootstrap-state.json")
        if (!file.exists()) return BootstrapStateSnapshot()
        return runCatching {
            json.decodeFromString(BootstrapStateSnapshot.serializer(), file.readText())
        }.getOrDefault(BootstrapStateSnapshot())
    }

    private fun writeBootstrapState(state: BootstrapStateSnapshot) {
        writeTextSafely(cacheFile("bootstrap-state.json"), json.encodeToString(BootstrapStateSnapshot.serializer(), state))
    }

    private fun readCheckpointState(): DesktopSyncCheckpointState {
        val file = cacheFile("sync-checkpoints.json")
        if (!file.exists()) return DesktopSyncCheckpointState()
        return runCatching {
            json.decodeFromString(DesktopSyncCheckpointState.serializer(), file.readText())
        }.getOrDefault(DesktopSyncCheckpointState())
    }

    private fun writeCheckpointState(state: DesktopSyncCheckpointState) {
        writeTextSafely(cacheFile("sync-checkpoints.json"), json.encodeToString(DesktopSyncCheckpointState.serializer(), state))
    }

    private fun cacheFile(fileName: String): File {
        require(fileName.isNotBlank() && '/' !in fileName && '\\' !in fileName) { "Invalid cache file name." }
        return File(cacheDir, fileName)
    }

    private fun writeTextSafely(file: File, value: String) {
        val temp = java.nio.file.Files.createTempFile(file.parentFile.toPath(), ".${file.name}.", ".tmp").toFile()
        try {
            temp.writeText(value)
            runCatching {
                java.nio.file.Files.move(
                    temp.toPath(),
                    file.toPath(),
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE,
                )
            }.getOrElse {
                java.nio.file.Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }
        } finally {
            if (temp.exists()) runCatching { temp.delete() }
        }
    }

    private fun bootstrapKey(scope: String, accountKey: String): String = "$scope:${accountKey.trim().lowercase(Locale.ROOT)}"

    private fun checkpointKey(ownerKey: String, tableName: String, scopeKey: String): String = listOf(
        ownerKey.trim().lowercase(Locale.ROOT),
        tableName.trim().lowercase(Locale.ROOT),
        scopeKey.ifBlank { SyncCheckpointDefaults.globalScope() }.trim().lowercase(Locale.ROOT),
    ).joinToString("|")

    private fun SyncCheckpoint.normalized(): SyncCheckpoint = copy(
        ownerKey = ownerKey.trim().lowercase(Locale.ROOT),
        tableName = tableName.trim().lowercase(Locale.ROOT),
        scopeKey = scopeKey.ifBlank { SyncCheckpointDefaults.globalScope() }.trim().lowercase(Locale.ROOT),
    )

    private companion object {
        val cacheFiles = listOf(
            "departments.json",
            "teachers.json",
            "sessions.json",
            "students.json",
            "role.json",
            "bootstrap-state.json",
            "sync-checkpoints.json",
        )

        fun defaultBaseDir(): File {
            val appId = System.getProperty("cms.desktop.appId").orEmpty().ifBlank { "shared" }
            val root = System.getenv("APPDATA") ?: System.getProperty("user.home")
            return File(root, "CMSDesktop/$appId")
        }

        fun migrateLegacySharedCache(baseDir: File, cacheDir: File) {
            val appId = baseDir.name
            if (appId == "shared") return
            val parentFile = baseDir.parentFile ?: return
            val legacyCacheDir = File(parentFile, "shared/cache")
            if (!legacyCacheDir.exists()) return
            for (fileName in cacheFiles) {
                val target = File(cacheDir, fileName)
                val source = File(legacyCacheDir, fileName)
                if (!target.exists() && source.exists()) {
                    runCatching { source.copyTo(target) }
                }
            }
        }
    }
}

@Serializable
private data class DesktopSyncCheckpointState(
    val entries: List<DesktopSyncCheckpointSnapshot> = emptyList(),
)

@Serializable
private data class DesktopSyncCheckpointSnapshot(
    val ownerKey: String,
    val tableName: String,
    val scopeKey: String,
    val lastUpdatedAt: String,
    val lastSuccessfulSyncAt: String,
) {
    val key: String get() = "$ownerKey|$tableName|$scopeKey"

    fun toDomain(): SyncCheckpoint = SyncCheckpoint(ownerKey, tableName, scopeKey, lastUpdatedAt, lastSuccessfulSyncAt)

    companion object {
        fun from(checkpoint: SyncCheckpoint): DesktopSyncCheckpointSnapshot = DesktopSyncCheckpointSnapshot(
            checkpoint.ownerKey,
            checkpoint.tableName,
            checkpoint.scopeKey,
            checkpoint.lastUpdatedAt,
            checkpoint.lastSuccessfulSyncAt,
        )
    }
}
