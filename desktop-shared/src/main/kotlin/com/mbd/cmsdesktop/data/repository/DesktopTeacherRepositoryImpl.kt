package com.mbd.cmsdesktop.data.repository

import com.mbd.cmscommon.auth.AdminUserProvisioner
import com.mbd.cmscommon.data.mapper.DesktopTeacherMapper
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.TeacherDto
import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.domain.model.TeacherStatus
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmsdesktop.data.cache.DesktopBootstrapSnapshotStore
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

/**
 * Desktop repos are always-online: [sync]/[syncSelf] re-fetch into an in-memory cache that is
 * seeded on startup from [DesktopBootstrapSnapshotStore] (JSON-file offline cache) and persisted
 * back to it after every mutation, so the last-known teacher roster survives a restart even
 * without connectivity.
 */
@Singleton
class DesktopTeacherRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val provisioner: AdminUserProvisioner,
    private val snapshotStore: DesktopBootstrapSnapshotStore,
) : TeacherRepository {

    private val cache = MutableStateFlow(snapshotStore.readTeachers().map { DesktopTeacherMapper.dtoToDomain(it) })

    override fun observeTeacher(teacherId: String): Flow<Teacher> =
        cache.asStateFlow().map { list -> list.find { it.teacherId == teacherId } }.filterNotNull()

    override fun observeActiveTeachers(): Flow<List<Teacher>> = cache.asStateFlow()

    override suspend fun getTeacher(teacherId: String): Teacher? = cache.value.find { it.teacherId == teacherId }

    override suspend fun resolveNameOrFallback(teacherId: String): String =
        getTeacher(teacherId)?.name ?: "Deleted Teacher"

    override suspend fun sync() {
        val rows = postgrest.from(SupabaseTables.TEACHERS).select().decodeList<TeacherDto>()
        snapshotStore.writeTeachers(rows)
        cache.value = rows.map { DesktopTeacherMapper.dtoToDomain(it) }
    }

    override suspend fun syncSelf(teacherId: String) {
        val dto = postgrest.from(SupabaseTables.TEACHERS).select { filter { eq("email", teacherId) } }
            .decodeList<TeacherDto>().firstOrNull() ?: return
        val teacher = DesktopTeacherMapper.dtoToDomain(dto)
        cache.value = cache.value.filterNot { it.teacherId == teacher.teacherId } + teacher
        snapshotStore.writeTeachers(cache.value.map { DesktopTeacherMapper.domainToDto(it) })
    }

    override suspend fun createTeacherAccount(email: String, password: String, teacher: Teacher) {
        val key = email.trim().lowercase()
        provisioner.createTeacher(key, password, teacher.name, teacher.deptId, teacher.designation, teacher.phone)
        val teacherWithId = teacher.copy(teacherId = key, email = key)
        postgrest.from(SupabaseTables.TEACHERS).upsert(DesktopTeacherMapper.domainToDto(teacherWithId)) { onConflict = "email" }
        sync()
    }

    override suspend fun updateTeacher(teacher: Teacher) {
        postgrest.from(SupabaseTables.TEACHERS).upsert(DesktopTeacherMapper.domainToDto(teacher)) { onConflict = "email" }
        sync()
    }

    override suspend fun deleteTeacher(teacherId: String) {
        provisioner.setTeacherStatus(teacherId, "DELETE")
        cache.value = cache.value.filterNot { it.teacherId == teacherId }
        snapshotStore.writeTeachers(cache.value.map { DesktopTeacherMapper.domainToDto(it) })
    }

    override suspend fun setStatus(teacherId: String, status: TeacherStatus) {
        provisioner.setTeacherStatus(teacherId, status.name)
        syncSelf(teacherId)
    }
}
