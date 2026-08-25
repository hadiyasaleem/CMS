package com.mbd.cmsdesktop.ui.teacher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmscommon.teacher.TeacherAssignmentsProvider
import com.mbd.cmscommon.ui.components.TeacherProfileWorkspace
import com.mbd.cmscommon.util.userMessage
import kotlinx.coroutines.launch

/** Profile leaf: syncs the signed-in teacher's own record and offers password reset / sign-out. */
@Composable
fun TeacherProfileScreen(
    teacherId: String,
    sessionManager: SessionManager,
    teacherRepository: TeacherRepository,
    departmentRepository: DepartmentRepository,
    assignmentsProvider: TeacherAssignmentsProvider,
    onSignOut: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val profile by teacherRepository.observeTeacher(teacherId).collectAsState(initial = null)
    val departments by departmentRepository.observeActiveDepartments().collectAsState(initial = emptyList())
    val assignments by assignmentsProvider.observeAssignmentsFor(teacherId).collectAsState(initial = emptyList())
    val departmentName = remember(profile, departments) {
        departments.firstOrNull { it.deptId == profile?.deptId }?.let { "${it.name} (${it.code})" }
    }

    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var actionMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(teacherId) {
        loading = true
        error = null
        runCatching { teacherRepository.syncSelf(teacherId) }
            .onFailure { error = it.userMessage() }
        loading = false
    }

    val accountKey = sessionManager.accountKey ?: teacherId

    TeacherProfileWorkspace(
        profile = profile,
        accountKey = accountKey,
        departmentName = departmentName,
        assignments = assignments,
        loading = loading,
        errorMessage = error,
        actionMessage = actionMessage,
        onResetPassword = {
            scope.launch {
                error = null
                actionMessage = null
                runCatching { sessionManager.sendPasswordReset(accountKey) }
                    .onSuccess { actionMessage = "Password reset link sent to $accountKey." }
                    .onFailure { error = it.userMessage() }
            }
        },
        onSignOut = onSignOut,
    )
}
