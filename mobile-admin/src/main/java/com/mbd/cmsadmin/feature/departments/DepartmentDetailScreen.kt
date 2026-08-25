package com.mbd.cmsadmin.feature.departments

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import com.mbd.cmscommon.ui.components.DepartmentDetailWorkspace

@Composable
fun DepartmentDetailScreen(
    deptId: String,
    onOpenSession: (String) -> Unit,
    viewModel: DepartmentDetailViewModel = hiltViewModel(),
) {
    val department by viewModel.department.collectAsState()
    val departmentName by viewModel.deptName.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val errorMessage by viewModel.error.collectAsState()
    val teachers by viewModel.teachers.collectAsState()
    val studentCounts = sessions.associate { session ->
        val count by remember(session.sessionId) {
            viewModel.observeStudentCount(session.sessionId)
        }.collectAsState(initial = 0)
        session.sessionId to count
    }

    DepartmentDetailWorkspace(
        department = department,
        fallbackName = departmentName.ifBlank { deptId },
        sessions = sessions,
        studentCounts = studentCounts,
        teachers = teachers,
        errorMessage = errorMessage,
        onOpenSession = onOpenSession,
        onCreateSession = viewModel::createSession,
        onUpdateDepartment = viewModel::updateDetails,
        onClearError = viewModel::clearError,
    )
}
