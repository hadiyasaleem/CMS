package com.mbd.cmsdesktop.ui.student

import kotlinx.coroutines.CancellationException
import com.mbd.cmscommon.util.userMessageLogged
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
    val loadError by controller.error.collectAsState()
    var resetMessage by remember { mutableStateOf<String?>(null) }
    var resetError by remember { mutableStateOf<String?>(null) }

    val accountKey = sessionManager.accountKey.orEmpty()
    var department by remember { mutableStateOf<Department?>(null) }
    LaunchedEffect(sessionId) {
        // Best-effort: the department name is only a label; the profile itself reports its own load failures.
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
        errorMessage = resetError ?: loadError,
        actionMessage = resetMessage,
        onRetry = controller::refresh,
        onResetPassword = {
            scope.launch {
                try {
                    sessionManager.sendPasswordReset(accountKey)
                    resetError = null
                    resetMessage = "Password reset email sent."
                } catch (c: CancellationException) {
                    throw c
                } catch (t: Throwable) {
                    resetMessage = null
                    resetError = t.userMessageLogged("StudentOwnProfileScreen.resetPassword", "Couldn't send the password reset email to $accountKey.")
                }
            }
        },
        onSignOut = onSignOut,
    )
}
