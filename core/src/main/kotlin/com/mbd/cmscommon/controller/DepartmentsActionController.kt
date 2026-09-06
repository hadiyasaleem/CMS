package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.util.FieldValidators
import com.mbd.cmscommon.util.orThrowValidation
import com.mbd.cmscommon.util.requireValid
import java.time.Instant
import java.util.Locale
import kotlinx.coroutines.CoroutineScope

class DepartmentsActionController(
    private val repo: DepartmentRepository,
    private val createdBy: String,
    scope: CoroutineScope,
) : ScreenController(scope) {

    fun create(name: String, code: String, hodEmail: String? = null, description: String? = null) = launch {
        FieldValidators.nameError(name, "Department name").orThrowValidation()
        val normalizedCode = code.trim().uppercase(Locale.ROOT)
        FieldValidators.departmentCodeError(normalizedCode).orThrowValidation()
        requireValid(FieldValidators.emailError(hodEmail ?: "", false) == null) { "Choose a valid head of department." }
        requireValid((description ?: "").trim().length <= 500) { "Department description must not exceed 500 characters." }

        val deptId = Regex("[^a-z0-9-]").replace(normalizedCode.lowercase(Locale.ROOT), "-")
        val now = Instant.now()
        repo.createDepartment(
            Department(
                deptId = deptId,
                name = name.trim(),
                code = normalizedCode,
                hodEmail = hodEmail?.trim()?.takeIf { it.isNotBlank() },
                description = description?.trim()?.takeIf { it.isNotBlank() },
                createdAt = now,
                createdBy = createdBy,
                updatedAt = now,
                updatedBy = createdBy,
            ),
        )
    }

    fun update(existing: Department, name: String, code: String, hodEmail: String?, description: String?) = launch {
        FieldValidators.nameError(name, "Department name").orThrowValidation()
        FieldValidators.departmentCodeError(code).orThrowValidation()
        requireValid(FieldValidators.emailError(hodEmail ?: "", false) == null) { "Choose a valid head of department." }
        requireValid((description ?: "").trim().length <= 500) { "Department description must not exceed 500 characters." }

        repo.updateDepartment(
            existing.copy(
                name = name.trim(),
                code = code.trim(),
                hodEmail = hodEmail?.trim()?.takeIf { it.isNotBlank() },
                description = description?.trim()?.takeIf { it.isNotBlank() },
                updatedAt = Instant.now(),
                updatedBy = createdBy,
            ),
        )
    }

    fun delete(deptId: String) = launch {
        repo.deleteDepartment(deptId)
    }
}
