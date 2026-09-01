package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.util.FieldValidators
import java.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class DepartmentDetailController(
    val deptId: String,
    private val departmentRepository: DepartmentRepository,
    private val sessionRepository: AcademicSessionRepository,
    private val editedBy: String,
    scope: CoroutineScope,
) : ScreenController(scope) {

    private val _department = MutableStateFlow<Department?>(null)
    val department: StateFlow<Department?> = _department.asStateFlow()

    val deptName: StateFlow<String> = _department
        .map { it?.name ?: deptId }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), deptId)

    val sessions: StateFlow<List<AcademicSession>> = sessionRepository.observeSessionsForDept(deptId)
        .map { departmentDetailSnapshot(it, emptyMap()).sessions }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        launch { _department.value = departmentRepository.getDepartment(deptId) }
    }

    fun createSession(startYear: Int, shift: Session) = launch {
        sessionRepository.createSession(deptId, startYear, shift)
    }

    fun updateDetails(name: String, code: String, hodEmail: String?, description: String?) {
        val current = _department.value ?: return
        launch {
            FieldValidators.nameError(name, "Department name", required = false)?.let { throw IllegalArgumentException(it) }
            FieldValidators.departmentCodeError(code)?.let { throw IllegalArgumentException(it) }
            require(FieldValidators.emailError(hodEmail ?: "", required = false) == null) {
                "Choose a valid head of department."
            }
            require((description ?: "").trim().length <= 500) {
                "Department description must not exceed 500 characters."
            }

            val updated = current.copy(
                name = name.trim(),
                code = code.trim(),
                hodEmail = hodEmail?.trim()?.takeIf { it.isNotBlank() },
                description = description?.trim()?.takeIf { it.isNotBlank() },
                updatedAt = Instant.now(),
                updatedBy = editedBy,
            )
            departmentRepository.updateDepartment(updated)
            _department.value = updated
        }
    }
}
