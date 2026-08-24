package com.mbd.cmscommon.domain.repository

import com.mbd.cmscommon.domain.model.Department
import kotlinx.coroutines.flow.Flow

interface DepartmentRepository {
    fun observeActiveDepartments(): Flow<List<Department>>
    suspend fun getDepartment(deptId: String): Department?
    suspend fun sync()
    suspend fun createDepartment(department: Department)
    suspend fun updateDepartment(department: Department)
    suspend fun deleteDepartment(deptId: String)
}
