package com.mbd.cmsdesktop.data.cache

import com.mbd.cmscommon.data.remote.dto.AcademicSessionDto
import com.mbd.cmscommon.data.remote.dto.DepartmentDto
import com.mbd.cmscommon.data.remote.dto.SessionStudentDto
import com.mbd.cmscommon.data.remote.dto.TeacherDto
import com.mbd.cmscommon.domain.model.UserRole
import java.io.File
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * Desktop's on-disk mirror of the reference data an app needs before it can show a home screen
 * (departments/teachers/sessions/students), plus the last resolved [UserRole] and a set of
 * "bootstrap already ran for this scope+account" markers — all as flat JSON files under
 * `<baseDir>/cache/` (departments.json, teachers.json, etc). There's no Room on desktop, so this (not a database) is the entire
 * offline cache; a relaunch reads these files back before hitting the network again.
 *
 * When a per-app `cms.desktop.appId` is introduced after the app previously ran under the
 * default "shared" scope, any cache files that already exist under `shared/cache` are copied over
 * once so the new per-app cache isn't empty on first launch.
 */
class DesktopBootstrapSnapshotStore(baseDir: File = defaultBaseDir()) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    private val cacheDir: File = File(baseDir, "cache").apply { mkdirs() }

    init {
        migrateLegacySharedCache(baseDir, cacheDir)
    }

    fun readDepartments(): List<DepartmentDto> = readList("departments.json", DepartmentDto.serializer())
    fun writeDepartments(rows: List<DepartmentDto>) = writeList("departments.json", DepartmentDto.serializer(), rows)

    fun readTeachers(): List<TeacherDto> = readList("teachers.json", TeacherDto.serializer())
    fun writeTeachers(rows: List<TeacherDto>) = writeList("teachers.json", TeacherDto.serializer(), rows)

    fun readSessions(): List<AcademicSessionDto> = readList("sessions.json", AcademicSessionDto.serializer())
    fun writeSessions(rows: List<AcademicSessionDto>) = writeList("sessions.json", AcademicSessionDto.serializer(), rows)

    fun readStudents(): List<SessionStudentDto> = readList("students.json", SessionStudentDto.serializer())
    fun writeStudents(rows: List<SessionStudentDto>) = writeList("students.json", SessionStudentDto.serializer(), rows)

    fun readRole(): UserRole? {
        val file = File(cacheDir, "role.json")
        if (!file.exists()) return null
        return runCatching { json.decodeFromString(RoleSnapshot.serializer(), file.readText()).toUserRole() }.getOrNull()
    }

    fun writeRole(role: UserRole?) {
        val file = File(cacheDir, "role.json")
        if (role == null) {
            runCatching { file.delete() }
            return
        }
        runCatching { file.writeText(json.encodeToString(RoleSnapshot.serializer(), RoleSnapshot.from(role))) }
    }

    fun isBootstrapComplete(scope: String, accountKey: String): Boolean =
        readBootstrapState().completedKeys.contains(bootstrapKey(scope, accountKey))

    fun markBootstrapComplete(scope: String, accountKey: String) {
        val current = readBootstrapState()
        writeBootstrapState(current.copy(completedKeys = current.completedKeys + bootstrapKey(scope, accountKey)))
    }

    private fun readBootstrapState(): BootstrapStateSnapshot {
        val file = File(cacheDir, "bootstrap-state.json")
        if (!file.exists()) return BootstrapStateSnapshot()
        return runCatching {
            json.decodeFromString(BootstrapStateSnapshot.serializer(), file.readText())
        }.getOrDefault(BootstrapStateSnapshot())
    }

    private fun writeBootstrapState(state: BootstrapStateSnapshot) {
        runCatching {
            File(cacheDir, "bootstrap-state.json").writeText(json.encodeToString(BootstrapStateSnapshot.serializer(), state))
        }
    }

    private fun bootstrapKey(scope: String, accountKey: String): String = "$scope:${accountKey.trim().lowercase()}"

    private fun <T> readList(fileName: String, serializer: KSerializer<T>): List<T> {
        val file = File(cacheDir, fileName)
        if (!file.exists()) return emptyList()
        return runCatching {
            json.decodeFromString(ListSerializer(serializer), file.readText())
        }.getOrDefault(emptyList())
    }

    private fun <T> writeList(fileName: String, serializer: KSerializer<T>, rows: List<T>) {
        runCatching {
            File(cacheDir, fileName).writeText(json.encodeToString(ListSerializer(serializer), rows))
        }
    }

    private companion object {
        val cacheFiles = listOf("departments.json", "teachers.json", "sessions.json", "students.json", "role.json", "bootstrap-state.json")

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
