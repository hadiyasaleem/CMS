package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.data.mapper.DesktopDepartmentMapper
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.DepartmentDto
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Desktop repos are always-online: no local persistence, `sync()` does a full re-fetch into an
 * in-memory [MutableStateFlow] cache that [observeActiveDepartments] just exposes. Every app
 * screen calls `sync()` explicitly (top bar refresh / on-open), matching the mobile RefreshBox
 * pattern but without a Room-backed offline cache.
 */
@Singleton
class DesktopDepartmentRepository @Inject constructor(
    private val postgrest: Postgrest,
) : DepartmentRepository {

    private val cache = MutableStateFlow<List<Department>>(emptyList())

    override fun observeActiveDepartments(): Flow<List<Department>> = cache.asStateFlow()

    override suspend fun getDepartment(deptId: String): Department? =
        cache.value.find { it.deptId == deptId } ?: postgrest.from(SupabaseTables.DEPARTMENTS).select {
            filter { eq("dept_id", deptId) }
        }.decodeSingleOrNull<DepartmentDto>()?.let { DesktopDepartmentMapper.dtoToDomain(it) }

    override suspend fun sync() {
        val rows = postgrest.from(SupabaseTables.DEPARTMENTS).select {
            filter { eq("is_active", true) }
            order("name", Order.ASCENDING)
        }.decodeList<DepartmentDto>()
        cache.value = rows.map { DesktopDepartmentMapper.dtoToDomain(it) }
    }

    override suspend fun createDepartment(department: Department) {
        postgrest.from(SupabaseTables.DEPARTMENTS).insert(DesktopDepartmentMapper.domainToDto(department))
        sync()
    }

    override suspend fun updateDepartment(department: Department) {
        postgrest.from(SupabaseTables.DEPARTMENTS).update(DesktopDepartmentMapper.domainToDto(department)) {
            filter { eq("dept_id", department.deptId) }
        }
        sync()
    }

    override suspend fun deleteDepartment(deptId: String) {
        postgrest.from(SupabaseTables.DEPARTMENTS).delete { filter { eq("dept_id", deptId) } }
        sync()
    }
}
