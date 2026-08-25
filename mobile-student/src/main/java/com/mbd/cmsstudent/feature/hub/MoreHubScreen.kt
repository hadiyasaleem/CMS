package com.mbd.cmsstudent.feature.hub

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.domain.model.CalendarViewerContext
import com.mbd.cmscommon.domain.model.CalendarViewerRole
import com.mbd.cmscommon.domain.model.DocumentViewerContext
import com.mbd.cmscommon.domain.model.DocumentViewerRole
import com.mbd.cmscommon.domain.model.NotificationTargetRole
import com.mbd.cmscommon.domain.model.StudentMoreSnapshot
import com.mbd.cmscommon.domain.model.studentMoreSnapshot
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.CalendarRepository
import com.mbd.cmscommon.domain.repository.DocumentRepository
import com.mbd.cmscommon.domain.repository.NotificationAudienceContext
import com.mbd.cmscommon.domain.repository.NotificationRepository
import com.mbd.cmscommon.domain.repository.SessionFeeRepository
import com.mbd.cmscommon.ui.components.StudentMoreDestination
import com.mbd.cmscommon.ui.components.StudentMoreWorkspace
import com.mbd.cmsstudent.R
import com.mbd.cmsstudent.feature.common.CurrentStudentProvider
import com.mbd.cmsstudent.navigation.StudentDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MoreHubViewModel @Inject constructor(
    currentStudentProvider: CurrentStudentProvider,
    private val calendarRepository: CalendarRepository,
    private val documentRepository: DocumentRepository,
    private val feeRepository: SessionFeeRepository,
    private val sessionRepository: AcademicSessionRepository,
    private val notificationRepository: NotificationRepository,
) : ViewModel() {

    private val _refreshTrigger = MutableStateFlow(0)

    val snapshot: StateFlow<StudentMoreSnapshot?> = currentStudentProvider.observeContext()
        .distinctUntilChangedBy { it?.studentId }
        .flatMapLatest { context ->
            if (context == null) {
                flowOf<StudentMoreSnapshot?>(null)
            } else {
                _refreshTrigger.map {
                    val events = runCatching { calendarRepository.getEvents() }.getOrDefault(emptyList())
                    val documents = runCatching { documentRepository.getDocuments() }.getOrDefault(emptyList())
                    val fee = runCatching { feeRepository.getSessionFee(context.sessionId) }.getOrNull()
                    val profile = runCatching { sessionRepository.getStudentProfile(context.sessionId, context.rollNumber) }.getOrNull()
                    val unread = runCatching {
                        notificationRepository.observeUnreadCount(
                            NotificationTargetRole.STUDENT,
                            NotificationAudienceContext(sessionId = context.sessionId, departmentId = context.deptId),
                        ).first()
                    }.getOrDefault(0)
                    studentMoreSnapshot(
                        events = events,
                        documents = documents,
                        fee = fee,
                        unreadNotifications = unread,
                        profile = profile,
                        viewer = CalendarViewerContext(CalendarViewerRole.STUDENT, context.deptId, setOf(context.sessionId)),
                        documentViewer = DocumentViewerContext(DocumentViewerRole.STUDENT, context.deptId),
                        today = LocalDate.now(),
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun refresh() {
        viewModelScope.launch { _refreshTrigger.value += 1 }
    }
}

@Composable
fun MoreHubScreen(onOpen: (String) -> Unit, onSignOut: () -> Unit, viewModel: MoreHubViewModel = hiltViewModel()) {
    val snapshot by viewModel.snapshot.collectAsState()

    StudentMoreWorkspace(
        heroPainter = painterResource(R.drawable.student_more_hero),
        snapshot = snapshot,
        loading = snapshot == null,
        errorMessage = null,
        onRetry = viewModel::refresh,
        onOpen = { destination ->
            onOpen(
                when (destination) {
                    StudentMoreDestination.CALENDAR -> StudentDestination.Events.route
                    StudentMoreDestination.DOCUMENTS -> StudentDestination.Documents.route
                    StudentMoreDestination.FEES -> StudentDestination.Fees.route
                    StudentMoreDestination.NOTIFICATIONS -> StudentDestination.Notifications.route
                    StudentMoreDestination.PROFILE -> StudentDestination.Profile.route
                },
            )
        },
        onSignOut = onSignOut,
    )
}
