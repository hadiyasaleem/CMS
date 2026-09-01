package com.mbd.cmsdesktop.data.repository

import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.data.mapper.DesktopDepartmentMapper
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.DepartmentDto
import com.mbd.cmscommon.data.sync.SyncCheckpointDefaults
import com.mbd.cmscommon.data.sync.fetchIncrementalDelta
import com.mbd.cmscommon.data.sync.mergeIncrementalDelta
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmsdesktop.data.cache.DesktopBootstrapSnapshotStore
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class DesktopDepartmentRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val snapshotStore: DesktopBootstrapSnapshotStore,
    private val sessionManager: SessionManager,
) : DepartmentRepository {
    private val cache = MutableStateFlow(cachedRows().map(DesktopDepartmentMapper::dtoToDomain))

    override fun observeActiveDepartments(): Flow<List<Department>> = cache.asStateFlow()
    override suspend fun getDepartment(deptId: String): Department? = cache.value.find { it.deptId == deptId }

    override suspend fun sync() {
        val delta = fetchIncrementalDelta(
            snapshotStore, ownerKey(), SupabaseTables.DEPARTMENTS,
            SyncCheckpointDefaults.globalScope(), DepartmentDto::updatedAt,
        ) { since, from, to ->
            postgrest.from(SupabaseTables.DEPARTMENTS).select {
                filter { gte("updated_at", since) }
                order("updated_at", Order.ASCENDING)
                order("id", Order.ASCENDING)
                range(from, to)
            }.decodeList()
        }
        writeMerged(delta)
    }

    override suspend fun createDepartment(department: Department) {
        val inserted = postgrest.from(SupabaseTables.DEPARTMENTS)
            .insert(DesktopDepartmentMapper.domainToDto(department)) { select() }
            .decodeList<DepartmentDto>()
        writeMerged(inserted)
    }

    override suspend fun updateDepartment(department: Department) = createDepartment(department)

    override suspend fun deleteDepartment(deptId: String) {
        postgrest.from(SupabaseTables.DEPARTMENTS).update({ set("is_deleted", true) }) {
            filter { eq("dept_id", deptId) }
        }
        writeMerged(listOf(DepartmentDto(deptId = deptId, isDeleted = true)))
    }

    private fun cachedRows() = snapshotStore.readDepartments().filterNot { it.isDeleted }

    private fun writeMerged(delta: List<DepartmentDto>) {
        val merged = snapshotStore.updateRows("departments.json", DepartmentDto.serializer()) { existing ->
            mergeIncrementalDelta(existing, delta, { it.deptId.orEmpty() }, DepartmentDto::isDeleted)
        }
        cache.value = merged.map(DesktopDepartmentMapper::dtoToDomain)
    }

    private fun ownerKey() =
        sessionManager.accountKey ?: SyncCheckpointDefaults.ownerKey("anonymous-local")
}
