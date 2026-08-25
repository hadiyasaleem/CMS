package com.mbd.cmsdesktop.data.repository

import com.mbd.cmscommon.data.mapper.DesktopDepartmentMapper
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.DepartmentDto
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmsdesktop.data.cache.DesktopBootstrapSnapshotStore
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Seeded on startup from [DesktopBootstrapSnapshotStore] so the department list survives a
 * restart without connectivity; [sync] does an unfiltered full re-fetch (every department,
 * active or not) and persists the fresh snapshot back. [getDepartment] only reads the in-memory
 * cache — unlike the department-scoped repos below it, it never falls back to a network call.
 */
@Singleton
class DesktopDepartmentRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val snapshotStore: DesktopBootstrapSnapshotStore,
) : DepartmentRepository {

    private val cache = MutableStateFlow(snapshotStore.readDepartments().map { DesktopDepartmentMapper.dtoToDomain(it) })

    override fun observeActiveDepartments(): Flow<List<Department>> = cache.asStateFlow()

    override suspend fun getDepartment(deptId: String): Department? = cache.value.find { it.deptId == deptId }

    override suspend fun sync() {
        val rows = postgrest.from(SupabaseTables.DEPARTMENTS).select().decodeList<DepartmentDto>()
        snapshotStore.writeDepartments(rows)
        cache.value = rows.map { DesktopDepartmentMapper.dtoToDomain(it) }
    }

    override suspend fun createDepartment(department: Department) {
        postgrest.from(SupabaseTables.DEPARTMENTS).upsert(DesktopDepartmentMapper.domainToDto(department))
        sync()
    }

    override suspend fun updateDepartment(department: Department) {
        createDepartment(department)
    }

    override suspend fun deleteDepartment(deptId: String) {
        postgrest.from(SupabaseTables.DEPARTMENTS).delete { filter { eq("dept_id", deptId) } }
        cache.value = cache.value.filterNot { it.deptId == deptId }
        snapshotStore.writeDepartments(cache.value.map { DesktopDepartmentMapper.domainToDto(it) })
    }
}
