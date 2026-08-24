package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.data.local.dao.DepartmentDao
import com.mbd.cmscommon.data.mapper.DepartmentMapper
import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.DepartmentDto
import com.mbd.cmscommon.data.sync.maxRemoteUpdatedAt
import com.mbd.cmscommon.data.sync.SyncCheckpoint
import com.mbd.cmscommon.data.sync.SyncCheckpointDefaults
import com.mbd.cmscommon.data.sync.SyncCheckpointStore
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DepartmentRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val departmentDao: DepartmentDao,
    private val checkpointStore: SyncCheckpointStore,
    private val sessionManager: SessionManager,
) : DepartmentRepository {

    override fun observeActiveDepartments(): Flow<List<Department>> =
        departmentDao.observeActive().map { rows -> rows.map { DepartmentMapper.entityToDomain(it) } }

    override suspend fun getDepartment(deptId: String): Department? =
        departmentDao.getById(deptId)?.let { DepartmentMapper.entityToDomain(it) }

    override suspend fun sync() {
        val ownerKey = SyncCheckpointDefaults.ownerKey(sessionManager.accountKey ?: "")
        val scopeKey = SyncCheckpointDefaults.globalScope()
        val checkpoint = checkpointStore.get(ownerKey, SupabaseTables.DEPARTMENTS, scopeKey)
        val since = checkpoint?.lastUpdatedAt ?: SyncCheckpointDefaults.EPOCH
        var maxUpdatedAt = since

        var offset = 0L
        while (true) {
            val page = postgrest.from(SupabaseTables.DEPARTMENTS).select {
                filter { gte("updated_at", since) }
                order("updated_at", Order.ASCENDING)
                range(offset, offset + PAGE_SIZE - 1)
            }.decodeList<DepartmentDto>()

            if (page.isEmpty()) break

            val entities = page.map { DepartmentMapper.dtoToEntity(it) }
            val (deleted, active) = entities.partition { it.isDeleted }
            departmentDao.applyDelta(active, deleted.map { it.deptId })
            maxUpdatedAt = page.maxRemoteUpdatedAt(maxUpdatedAt) { it.updatedAt }

            if (page.size < PAGE_SIZE) break
            offset += PAGE_SIZE
        }

        checkpointStore.upsert(
            SyncCheckpoint(
                ownerKey = ownerKey,
                tableName = SupabaseTables.DEPARTMENTS,
                scopeKey = scopeKey,
                lastUpdatedAt = maxUpdatedAt,
                lastSuccessfulSyncAt = PgTime.format(java.time.Instant.now()) ?: since,
            ),
        )
    }

    override suspend fun createDepartment(department: Department) {
        postgrest.from(SupabaseTables.DEPARTMENTS).upsert(DepartmentMapper.domainToDto(department))
        departmentDao.upsert(DepartmentMapper.domainToEntity(department))
    }

    override suspend fun updateDepartment(department: Department) = createDepartment(department)

    override suspend fun deleteDepartment(deptId: String) {
        postgrest.from(SupabaseTables.DEPARTMENTS).update({ set("is_deleted", true) }) {
            filter { eq("dept_id", deptId) }
        }
        departmentDao.deleteById(deptId)
    }

    private companion object {
        const val PAGE_SIZE = 500L
    }
}
