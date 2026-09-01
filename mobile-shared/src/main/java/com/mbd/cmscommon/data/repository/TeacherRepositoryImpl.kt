package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.auth.AdminUserProvisioner
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.data.local.dao.TeacherDao
import com.mbd.cmscommon.data.mapper.TeacherMapper
import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.TeacherDto
import com.mbd.cmscommon.data.sync.SyncCheckpoint
import com.mbd.cmscommon.data.sync.SyncCheckpointDefaults
import com.mbd.cmscommon.data.sync.SyncCheckpointStore
import com.mbd.cmscommon.data.sync.maxRemoteUpdatedAt
import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.domain.model.TeacherStatus
import com.mbd.cmscommon.domain.repository.TeacherRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TeacherRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val teacherDao: TeacherDao,
    private val provisioner: AdminUserProvisioner,
    private val checkpointStore: SyncCheckpointStore,
    private val sessionManager: SessionManager,
) : TeacherRepository {

    private fun syncOwnerKey(): String = sessionManager.accountKey ?: SyncCheckpointDefaults.ownerKey("anonymous-local")

    override fun observeTeacher(teacherId: String): Flow<Teacher> =
        teacherDao.observe(teacherId).map { TeacherMapper.entityToDomain(it) }

    override fun observeActiveTeachers(): Flow<List<Teacher>> =
        teacherDao.observeActive().map { rows -> rows.map { TeacherMapper.entityToDomain(it) } }

    override suspend fun getTeacher(teacherId: String): Teacher? =
        teacherDao.getById(teacherId)?.let { TeacherMapper.entityToDomain(it) }

    override suspend fun resolveNameOrFallback(teacherId: String): String =
        teacherDao.getById(teacherId)?.name ?: "Deleted Teacher"

    override suspend fun sync() {
        val ownerKey = syncOwnerKey()
        val scopeKey = SyncCheckpointDefaults.globalScope()
        val checkpoint = checkpointStore.get(ownerKey, SupabaseTables.TEACHERS, scopeKey)
        val since = checkpoint?.lastUpdatedAt ?: SyncCheckpointDefaults.EPOCH
        var maxUpdatedAt = since

        var offset = 0L
        while (true) {
            val page = postgrest.from(SupabaseTables.TEACHERS).select {
                filter { gte("updated_at", since) }
                order("updated_at", Order.ASCENDING)
                range(offset, offset + PAGE_SIZE - 1)
            }.decodeList<TeacherDto>()
            if (page.isEmpty()) break

            val entities = page.map { TeacherMapper.dtoToEntity(it) }
            val (deleted, active) = entities.partition { it.isDeleted }
            teacherDao.applyDelta(active, deleted.map { it.teacherId })
            maxUpdatedAt = page.maxRemoteUpdatedAt(maxUpdatedAt) { it.updatedAt }

            if (page.size < PAGE_SIZE) break
            offset += PAGE_SIZE
        }

        checkpointStore.upsert(SyncCheckpoint(ownerKey, SupabaseTables.TEACHERS, scopeKey, maxUpdatedAt, PgTime.format(Instant.now()) ?: since))
    }

    override suspend fun syncSelf(teacherId: String) {
        val dto = postgrest.from(SupabaseTables.TEACHERS).select { filter { eq("email", teacherId) } }
            .decodeList<TeacherDto>().firstOrNull() ?: return
        teacherDao.upsert(TeacherMapper.dtoToEntity(dto))
    }

    override suspend fun createTeacherAccount(email: String, password: String, teacher: Teacher) {
        val key = email.trim().lowercase()
        provisioner.createTeacher(key, password, teacher.name, teacher.deptId, teacher.designation, teacher.phone)
        val teacherWithId = teacher.copy(teacherId = key, email = key)
        postgrest.from(SupabaseTables.TEACHERS).upsert(TeacherMapper.domainToDto(teacherWithId)) { onConflict = "email" }
        teacherDao.upsert(TeacherMapper.domainToEntity(teacherWithId))
    }

    override suspend fun updateTeacher(teacher: Teacher) {
        postgrest.from(SupabaseTables.TEACHERS).upsert(TeacherMapper.domainToDto(teacher)) { onConflict = "email" }
        teacherDao.upsert(TeacherMapper.domainToEntity(teacher))
    }

    override suspend fun deleteTeacher(teacherId: String) {
        provisioner.setTeacherStatus(teacherId, "DELETE")
        teacherDao.deleteById(teacherId)
    }

    override suspend fun setStatus(teacherId: String, status: TeacherStatus) {
        provisioner.setTeacherStatus(teacherId, status.name)
        teacherDao.getById(teacherId)?.let { cached ->
            teacherDao.upsert(
                cached.copy(
                    status = status.name,
                    isActive = status == TeacherStatus.ACTIVE,
                    updatedAt = System.currentTimeMillis(),
                ),
            )
        }
    }

    private companion object {
        const val PAGE_SIZE = 500L
    }
}
