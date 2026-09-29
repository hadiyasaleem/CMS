package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.util.CmsException
import com.mbd.cmscommon.util.FieldValidators
import com.mbd.cmscommon.util.orThrowValidation
import com.mbd.cmscommon.util.previewText
import com.mbd.cmscommon.util.requireValid
import java.time.Instant
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first

class DepartmentsActionController(
    private val repo: DepartmentRepository,
    private val createdBy: String,
    scope: CoroutineScope,
    /** When given, deleting a department that still has sessions is refused with the sessions named. */
    private val sessionRepository: AcademicSessionRepository? = null,
) : ScreenController(scope) {

    fun create(name: String, code: String, hodEmail: String? = null, description: String? = null) = launch("create the department") {
        FieldValidators.nameError(name, "Department name").orThrowValidation()
        val normalizedCode = code.trim().uppercase(Locale.ROOT)
        FieldValidators.departmentCodeError(normalizedCode).orThrowValidation()
        requireValid(FieldValidators.emailError(hodEmail ?: "", false) == null) { "Choose a valid head of department." }
        requireValid((description ?: "").trim().length <= 500) { "Department description must not exceed 500 characters." }

        val deptId = Regex("[^a-z0-9-]").replace(normalizedCode.lowercase(Locale.ROOT), "-")
        rejectDuplicate(name, normalizedCode, deptId, exceptDeptId = null)
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

    fun update(existing: Department, name: String, code: String, hodEmail: String?, description: String?) = launch("update the department") {
        FieldValidators.nameError(name, "Department name").orThrowValidation()
        FieldValidators.departmentCodeError(code).orThrowValidation()
        requireValid(FieldValidators.emailError(hodEmail ?: "", false) == null) { "Choose a valid head of department." }
        requireValid((description ?: "").trim().length <= 500) { "Department description must not exceed 500 characters." }

        rejectDuplicate(name, code.trim().uppercase(Locale.ROOT), deptId = null, exceptDeptId = existing.deptId)

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

    fun delete(deptId: String) = launch("delete the department") {
        val sessions = sessionRepository?.observeSessionsForDept(deptId)?.first().orEmpty()
        if (sessions.isNotEmpty()) {
            val label = repo.getDepartment(deptId)?.code ?: "This department"
            val names = sessions.map { "${it.startYear}–${it.endYear}" }.previewText()
            throw CmsException.Conflict("$label still has ${sessions.size} session(s) ($names). Delete or graduate those sessions first.")
        }
        repo.deleteDepartment(deptId)
    }

    /** A department is identified by its code and named for people: neither may repeat another active department. */
    private suspend fun rejectDuplicate(name: String, code: String, deptId: String?, exceptDeptId: String?) {
        val others = repo.observeActiveDepartments().first().filter { it.deptId != exceptDeptId }
        others.firstOrNull { it.code.equals(code, ignoreCase = true) || (deptId != null && it.deptId == deptId) }?.let {
            throw CmsException.Conflict("A department with code ${it.code} already exists (${it.name}).")
        }
        others.firstOrNull { it.name.trim().equals(name.trim(), ignoreCase = true) }?.let {
            throw CmsException.Conflict("A department named \"${it.name}\" already exists (code ${it.code}).")
        }
    }
}
