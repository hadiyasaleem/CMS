package com.mbd.cmsstudent.feature.linkrequest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.domain.model.StudentLinkRequest
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.StudentLinkRequestRepository
import com.mbd.cmscommon.domain.repository.UserRepository
import com.mbd.cmscommon.ui.components.StudentLinkRequestUiState
import com.mbd.cmscommon.util.Outcome
import com.mbd.cmscommon.util.userMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class LinkRequestViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val linkRequestRepository: StudentLinkRequestRepository,
    private val departmentRepository: DepartmentRepository,
    private val sessionRepository: AcademicSessionRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _refreshing = MutableStateFlow(false)
    private val _refreshError = MutableStateFlow<String?>(null)
    private val _submitState = MutableStateFlow<Outcome<Unit>>(Outcome.Success(Unit))

    private val latestRequest: Flow<StudentLinkRequest?> =
        linkRequestRepository.observeRequestsForStudentUid(sessionManager.accountKey.orEmpty())
            .map { requests -> requests.maxByOrNull { it.createdAt } }

    private val referenceData: Flow<Triple<List<com.mbd.cmscommon.domain.model.Department>, List<com.mbd.cmscommon.domain.model.AcademicSession>, StudentLinkRequest?>> = combine(
        departmentRepository.observeActiveDepartments(),
        sessionRepository.observeAllSessions(),
        latestRequest,
    ) { departments, sessions, request -> Triple(departments, sessions, request) }

    private val transientState: Flow<Triple<Boolean, String?, Outcome<Unit>>> = combine(_refreshing, _refreshError, _submitState) { refreshing, refreshError, submitState ->
        Triple(refreshing, refreshError, submitState)
    }

    val uiState: StateFlow<StudentLinkRequestUiState> = combine(referenceData, transientState) { reference, transient ->
        StudentLinkRequestUiState(
            departments = reference.first,
            sessions = reference.second,
            latestRequest = reference.third,
            submitState = transient.third,
            refreshing = transient.first,
            refreshError = transient.second,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StudentLinkRequestUiState())

    fun refresh() {
        _refreshing.value = true
        viewModelScope.launch {
            try {
                coroutineScope {
                    listOf(
                        async { linkRequestRepository.sync() },
                        async { departmentRepository.sync() },
                    ).awaitAll()
                }
                // Session lists are per-department, so every active department needs its own sync
                // once departments themselves are current -- otherwise the session picker stays empty.
                val departments = departmentRepository.observeActiveDepartments().first()
                coroutineScope {
                    departments.map { department -> async { sessionRepository.syncSessionsForDept(department.deptId) } }.awaitAll()
                }
                // A prior request may have been approved on the admin's device since this screen last
                // loaded -- re-resolve this account's role so approval actually hands off to the linked
                // student workspace instead of just updating the (now-stale) request status shown here.
                sessionManager.accountKey?.let { accountKey -> runCatching { userRepository.resolveRole(accountKey) } }
                _refreshError.value = null
            } catch (t: Throwable) {
                _refreshError.value = t.userMessage("Refresh failed. Please try again.")
            } finally {
                _refreshing.value = false
            }
        }
    }

    fun submit(sessionId: String, rollNumber: String, name: String, cnic: String, dob: String, universityRoll: String, registrationNo: String, message: String) {
        viewModelScope.launch {
            _submitState.value = Outcome.Loading
            try {
                linkRequestRepository.submitRequest(
                    sessionId = sessionId,
                    rollNumber = rollNumber,
                    name = name,
                    cnic = cnic,
                    dob = dob,
                    universityRoll = universityRoll.ifBlank { null },
                    registrationNo = registrationNo.ifBlank { null },
                    message = message.ifBlank { null },
                    requestedByUid = sessionManager.accountKey,
                )
                _submitState.value = Outcome.Success(Unit)
            } catch (t: Throwable) {
                _submitState.value = Outcome.Error(t.userMessage("Could not submit your request."), t)
            }
        }
    }
}
