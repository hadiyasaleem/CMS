package com.mbd.cmsadmin.feature.departments

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.controller.DepartmentDetailController
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class DepartmentDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    departmentRepository: DepartmentRepository,
    private val sessionRepository: AcademicSessionRepository,
    teacherRepository: TeacherRepository,
    sessionManager: SessionManager,
) : ViewModel() {

    private val controller = DepartmentDetailController(
        deptId = checkNotNull(savedStateHandle["deptId"]),
        departmentRepository = departmentRepository,
        sessionRepository = sessionRepository,
        editedBy = sessionManager.accountKey.orEmpty(),
        scope = viewModelScope,
    )

    val deptId: String = controller.deptId
    val deptName = controller.deptName
    val department = controller.department
    val sessions = controller.sessions
    val error = controller.error
    val teachers = teacherRepository.observeActiveTeachers()
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000), emptyList())

    fun createSession(startYear: Int, shift: Session) = controller.createSession(startYear, shift)
    fun observeStudentCount(sessionId: String) =
        sessionRepository.observeStudents(sessionId).map { it.size }

    fun updateDetails(name: String, code: String, hodEmail: String?, description: String?) =
        controller.updateDetails(name, code, hodEmail, description)
    fun clearError() = controller.clearError()
}
