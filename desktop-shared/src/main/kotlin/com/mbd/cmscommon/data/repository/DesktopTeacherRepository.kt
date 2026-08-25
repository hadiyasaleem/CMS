package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.auth.AdminUserProvisioner
import com.mbd.cmscommon.data.mapper.DesktopTeacherMapper
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.TeacherDto
import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.domain.model.TeacherStatus
import com.mbd.cmscommon.domain.repository.TeacherRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

/**
 * Desktop repos are always-online: no local persistence, `sync()`/`syncSelf()` re-fetch into an
 * in-memory cache keyed by teacherId (email) that the `observe*` methods just filter and expose.
 */
@Singleton
class DesktopTeacherRepository @Inject constructor(
    private val postgrest: Postgrest,
    private val provisioner: AdminUserProvisioner,
) : TeacherRepository {

    private val cache = MutableStateFlow<Map<String, Teacher>>(emptyMap())

    override fun observeTeacher(teacherId: String): Flow<Teacher> =
        cache.map { it[teacherId] }.filterNotNull()

    override fun observeActiveTeachers(): Flow<List<Teacher>> =
        cache.map { m -> m.values.filter { it.isActive }.sortedBy { it.name } }

    override suspend fun getTeacher(teacherId: String): Teacher? = cache.value[teacherId]

    override suspend fun resolveNameOrFallback(teacherId: String): String =
        cache.value[teacherId]?.name ?: "Deleted Teacher"

    override suspend fun sync() {
        val rows = postgrest.from(SupabaseTables.TEACHERS).select {
            order("name", Order.ASCENDING)
        }.decodeList<TeacherDto>()
        cache.value = rows.associate { val t = DesktopTeacherMapper.dtoToDomain(it); t.teacherId to t }
    }

    override suspend fun syncSelf(teacherId: String) {
        val dto = postgrest.from(SupabaseTables.TEACHERS).select { filter { eq("email", teacherId) } }
            .decodeList<TeacherDto>().firstOrNull() ?: return
        val teacher = DesktopTeacherMapper.dtoToDomain(dto)
        cache.value = cache.value + (teacher.teacherId to teacher)
    }

    override suspend fun createTeacherAccount(email: String, password: String, teacher: Teacher) {
        val key = email.trim().lowercase()
        provisioner.createTeacher(key, password, teacher.name, teacher.deptId, teacher.designation, teacher.phone)
        val teacherWithId = teacher.copy(teacherId = key, email = key)
        postgrest.from(SupabaseTables.TEACHERS).upsert(DesktopTeacherMapper.domainToDto(teacherWithId)) { onConflict = "email" }
        syncSelf(key)
    }

    override suspend fun updateTeacher(teacher: Teacher) {
        postgrest.from(SupabaseTables.TEACHERS).upsert(DesktopTeacherMapper.domainToDto(teacher)) { onConflict = "email" }
        syncSelf(teacher.teacherId)
    }

    override suspend fun deleteTeacher(teacherId: String) {
        provisioner.setTeacherStatus(teacherId, "DELETE")
        cache.value = cache.value - teacherId
    }

    override suspend fun setStatus(teacherId: String, status: TeacherStatus) {
        provisioner.setTeacherStatus(teacherId, status.name)
        syncSelf(teacherId)
    }
}
