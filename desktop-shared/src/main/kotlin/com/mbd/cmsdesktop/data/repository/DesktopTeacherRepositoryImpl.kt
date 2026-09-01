package com.mbd.cmsdesktop.data.repository

import com.mbd.cmscommon.auth.AdminUserProvisioner
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.data.mapper.DesktopTeacherMapper
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.TeacherDto
import com.mbd.cmscommon.data.sync.SyncCheckpointDefaults
import com.mbd.cmscommon.data.sync.fetchIncrementalDelta
import com.mbd.cmscommon.data.sync.mergeIncrementalDelta
import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.domain.model.TeacherStatus
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmsdesktop.data.cache.DesktopBootstrapSnapshotStore
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

@Singleton
class DesktopTeacherRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val provisioner: AdminUserProvisioner,
    private val snapshotStore: DesktopBootstrapSnapshotStore,
    private val sessionManager: SessionManager,
) : TeacherRepository {
    private val cache = MutableStateFlow(cachedRows().map(DesktopTeacherMapper::dtoToDomain))

    override fun observeTeacher(teacherId: String): Flow<Teacher> =
        cache.asStateFlow().map { list -> list.find { it.teacherId == teacherId } }.filterNotNull()

    override fun observeActiveTeachers(): Flow<List<Teacher>> = cache.asStateFlow()
    override suspend fun getTeacher(teacherId: String): Teacher? = cache.value.find { it.teacherId == teacherId }
    override suspend fun resolveNameOrFallback(teacherId: String): String = getTeacher(teacherId)?.name ?: "Deleted Teacher"

    override suspend fun sync() {
        val delta = fetchIncrementalDelta(
            snapshotStore, ownerKey(), SupabaseTables.TEACHERS,
            SyncCheckpointDefaults.globalScope(), TeacherDto::updatedAt,
        ) { since, from, to ->
            postgrest.from(SupabaseTables.TEACHERS).select {
                filter { gte("updated_at", since) }
                order("updated_at", Order.ASCENDING)
                range(from, to)
            }.decodeList()
        }
        writeMerged(delta)
    }

    override suspend fun syncSelf(teacherId: String) {
        val rows = postgrest.from(SupabaseTables.TEACHERS).select {
            filter { eq("email", teacherId) }
        }.decodeList<TeacherDto>()
        writeMerged(rows)
    }

    override suspend fun createTeacherAccount(email: String, password: String, teacher: Teacher) {
        val key = email.trim().lowercase()
        provisioner.createTeacher(key, password, teacher.name, teacher.deptId, teacher.designation, teacher.phone)
        val rows = postgrest.from(SupabaseTables.TEACHERS).upsert(
            DesktopTeacherMapper.domainToDto(teacher.copy(teacherId = key, email = key)),
        ) {
            onConflict = "email"
            select()
        }.decodeList<TeacherDto>()
        writeMerged(rows)
    }

    override suspend fun updateTeacher(teacher: Teacher) {
        val rows = postgrest.from(SupabaseTables.TEACHERS)
            .upsert(DesktopTeacherMapper.domainToDto(teacher)) {
                onConflict = "email"
                select()
            }.decodeList<TeacherDto>()
        writeMerged(rows)
    }

    override suspend fun deleteTeacher(teacherId: String) {
        provisioner.setTeacherStatus(teacherId, "DELETE")
        snapshotStore.writeTeachers(cachedRows().filterNot { it.email == teacherId })
        reloadCache()
    }

    override suspend fun setStatus(teacherId: String, status: TeacherStatus) {
        provisioner.setTeacherStatus(teacherId, status.name)
        cachedRows().firstOrNull { it.email == teacherId }?.let { cached ->
            writeMerged(
                listOf(
                    cached.copy(
                        status = status.name,
                        isActive = status == TeacherStatus.ACTIVE,
                        updatedAt = Instant.now().toString(),
                    ),
                ),
            )
        }
    }

    private fun cachedRows() = snapshotStore.readTeachers().filterNot { it.isDeleted }

    private fun writeMerged(delta: List<TeacherDto>) {
        val merged = mergeIncrementalDelta(
            snapshotStore.readTeachers(), delta, { it.email.orEmpty() }, TeacherDto::isDeleted,
        )
        snapshotStore.writeTeachers(merged)
        cache.value = merged.map(DesktopTeacherMapper::dtoToDomain)
    }

    private fun reloadCache() {
        cache.value = cachedRows().map(DesktopTeacherMapper::dtoToDomain)
    }

    private fun ownerKey() =
        sessionManager.accountKey ?: SyncCheckpointDefaults.ownerKey("anonymous-local")
}
