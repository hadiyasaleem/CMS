package com.mbd.cmsdesktop.ui.student

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.mbd.cmscommon.domain.model.UserRole
import com.mbd.cmscommon.ui.components.StudentLinkRequestActions
import com.mbd.cmscommon.ui.components.StudentLinkRequestUiState
import com.mbd.cmscommon.ui.components.StudentLinkRequestWorkspace
import com.mbd.cmscommon.util.Outcome
import com.mbd.cmscommon.util.userMessage
import com.mbd.cmsdesktop.di.DesktopAppComponent
import kotlinx.coroutines.launch

/**
 * Shown when a signed-in student account has not yet been linked to a real student record. Unlike
 * mobile (whose `LinkRequestScreen` is the only entry point), desktop has its own full
 * submit-a-claim flow via [StudentLinkRequestWorkspace] - so a desktop-only student never has to
 * install the mobile app to get linked. After [DesktopAppComponent.studentLinkRequestRepository]
 * syncs and the account's role resolves to [UserRole.LinkedStudent], [onLinked] fires to move the
 * caller into the normal student shell.
 */
@Composable
fun StudentLinkRequestScreen(component: DesktopAppComponent, onLinked: (UserRole.LinkedStudent) -> Unit) {
    val scope = rememberCoroutineScope()
    val accountKey = component.sessionManager().accountKey.orEmpty()

    val departments by component.departmentRepository().observeActiveDepartments().collectAsState(initial = emptyList())
    val sessions by component.academicSessionRepository().observeAllSessions().collectAsState(initial = emptyList())
    val requests by component.studentLinkRequestRepository().observeRequestsForStudentUid(accountKey).collectAsState(initial = emptyList())

    var submitState by remember { mutableStateOf<Outcome<Unit>>(Outcome.Success(Unit)) }
    var refreshing by remember { mutableStateOf(false) }
    var refreshError by remember { mutableStateOf<String?>(null) }

    suspend fun refresh() {
        refreshing = true
        refreshError = null
        try {
            component.studentLinkRequestRepository().sync()
            val resolved = component.userRepository().resolveRole(accountKey)
            if (resolved is UserRole.LinkedStudent) {
                onLinked(resolved)
                return
            }
        } catch (t: Throwable) {
            refreshError = t.userMessage("Could not check your request status. Cached information is still shown.")
        } finally {
            refreshing = false
        }
    }

    val latestRequest = requests.maxByOrNull { it.createdAt }
    val state = StudentLinkRequestUiState(
        departments = departments,
        sessions = sessions,
        latestRequest = latestRequest,
        submitState = submitState,
        refreshing = refreshing,
        refreshError = refreshError,
    )
    val actions = StudentLinkRequestActions(
        onRefresh = { scope.launch { refresh() } },
        onSubmit = { sessionId, roll, name, cnic, dob, universityRoll, registrationNo, message ->
            scope.launch {
                submitState = Outcome.Loading
                try {
                    component.studentLinkRequestRepository().submitRequest(
                        sessionId = sessionId,
                        rollNumber = roll,
                        name = name,
                        cnic = cnic,
                        dob = dob,
                        universityRoll = universityRoll.ifBlank { null },
                        registrationNo = registrationNo.ifBlank { null },
                        message = message.ifBlank { null },
                        requestedByUid = accountKey,
                    )
                    submitState = Outcome.Success(Unit)
                    refresh()
                } catch (t: Throwable) {
                    submitState = Outcome.Error(t.userMessage("Could not submit your request. Please try again."), t)
                }
            }
        },
    )

    StudentLinkRequestWorkspace(state = state, actions = actions)
}
