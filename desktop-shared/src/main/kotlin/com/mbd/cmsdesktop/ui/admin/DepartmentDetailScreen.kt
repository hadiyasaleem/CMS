package com.mbd.cmsdesktop.ui.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.mbd.cmscommon.controller.DepartmentDetailController
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmscommon.ui.components.DepartmentDetailWorkspace
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf

@Composable
fun DepartmentDetailScreen(
    deptId: String,
    departmentRepository: DepartmentRepository,
    sessionRepository: AcademicSessionRepository,
    teacherRepository: TeacherRepository,
    editedBy: String?,
    onOpenSession: (String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(deptId, departmentRepository, sessionRepository, editedBy) {
        DepartmentDetailController(deptId, departmentRepository, sessionRepository, editedBy.orEmpty(), scope)
    }
    val department by controller.department.collectAsState()
    val departmentName by controller.deptName.collectAsState()
    val sessions by controller.sessions.collectAsState()
    val errorMessage by controller.error.collectAsState()
    val teachers by teacherRepository.observeActiveTeachers().collectAsState(initial = emptyList())

    val studentCountsFlow = remember(sessions) {
        if (sessions.isEmpty()) {
            flowOf(emptyMap())
        } else {
            combine(sessions.map { s -> sessionRepository.observeStudents(s.sessionId) }) { lists ->
                sessions.mapIndexed { index, s -> s.sessionId to lists[index].size }.toMap()
            }
        }
    }
    val studentCounts by studentCountsFlow.collectAsState(initial = emptyMap())

    DepartmentDetailWorkspace(
        department = department,
        fallbackName = departmentName.ifBlank { deptId },
        sessions = sessions,
        studentCounts = studentCounts,
        teachers = teachers,
        errorMessage = errorMessage,
        onOpenSession = onOpenSession,
        onCreateSession = controller::createSession,
        onUpdateDepartment = controller::updateDetails,
        onClearError = controller::clearError,
    )
}
