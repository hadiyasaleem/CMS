package com.mbd.cmsdesktop.ui.student

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.controller.StudentProfileController
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.FineRepository
import com.mbd.cmscommon.ui.components.StudentOwnProfileWorkspace
import com.mbd.cmscommon.util.StudentIdCodec
import kotlinx.coroutines.launch

/** Self-contained Profile leaf for the student desktop app: builds its own [StudentProfileController]. */
@Composable
fun StudentOwnProfileScreen(
    sessionId: String,
    rollNumber: String,
    sessionManager: SessionManager,
    sessionRepository: AcademicSessionRepository,
    departmentRepository: DepartmentRepository,
    fineRepository: FineRepository,
    onSignOut: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(sessionId, rollNumber) {
        StudentProfileController(sessionId, rollNumber, sessionRepository, fineRepository, scope)
    }
    val session by controller.session.collectAsState()
    val me by controller.me.collectAsState()
    val profile by controller.profile.collectAsState()
    val fines by controller.fines.collectAsState()
    val loading by controller.loading.collectAsState()

    val accountKey = sessionManager.accountKey.orEmpty()
    var department by remember { mutableStateOf<Department?>(null) }
    LaunchedEffect(sessionId) {
        department = runCatching { departmentRepository.getDepartment(StudentIdCodec.deptIdOf(sessionId)) }.getOrNull()
    }

    StudentOwnProfileWorkspace(
        session = session,
        studentName = me?.name ?: rollNumber,
        rollNumber = rollNumber,
        gpa = me?.gpa,
        cgpa = me?.cgpa,
        linkedEmail = accountKey,
        profile = profile,
        departmentName = department?.name,
        accountKey = accountKey,
        fines = fines,
        loading = loading && me == null,
        errorMessage = null,
        actionMessage = null,
        onRetry = controller::refresh,
        onResetPassword = { scope.launch { runCatching { sessionManager.sendPasswordReset(accountKey) } } },
        onSignOut = onSignOut,
    )
}
