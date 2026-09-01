package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.Notification
import com.mbd.cmscommon.domain.model.NotificationTargetRole
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.NotificationAudienceContext
import com.mbd.cmscommon.domain.repository.NotificationRepository
import com.mbd.cmscommon.teacher.ResolvedAssignment
import com.mbd.cmscommon.util.userMessage
import java.time.Instant
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class NotificationsController(
    private val repository: NotificationRepository,
    val viewerRole: NotificationTargetRole,
    private val accountKey: String,
    sessionRepository: AcademicSessionRepository,
    departmentRepository: DepartmentRepository,
    audienceContext: Flow<NotificationAudienceContext> = flowOf(NotificationAudienceContext()),
    private val publisherKind: NotificationPublisherKind = NotificationPublisherKind.NONE,
    private val permissionCheck: (suspend () -> Boolean)? = null,
    teacherAssignments: Flow<List<ResolvedAssignment>> = flowOf(emptyList()),
    scope: CoroutineScope,
) : ScreenController(scope) {

    companion object {
        const val SEND_ACTION = "send"
    }

    val context: StateFlow<NotificationAudienceContext> = audienceContext
        .distinctUntilChanged()
        .stateIn(scope, SharingStarted.Eagerly, NotificationAudienceContext())

    val inbox: StateFlow<List<Notification>> = context
        .flatMapLatest { repository.observeForRole(viewerRole, it) }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    val sent: StateFlow<List<Notification>> =
        if (publisherKind == NotificationPublisherKind.NONE) {
            MutableStateFlow(emptyList())
        } else {
            repository.observeAuthoredByCurrentUser(accountKey).stateIn(scope, SharingStarted.Eagerly, emptyList())
        }

    val departments: StateFlow<List<Department>> =
        departmentRepository.observeActiveDepartments().stateIn(scope, SharingStarted.Eagerly, emptyList())

    val sessions: StateFlow<List<AcademicSession>> =
        sessionRepository.observeAllSessions().stateIn(scope, SharingStarted.Eagerly, emptyList())

    val publishSessions: StateFlow<List<AcademicSession>> = when (publisherKind) {
        NotificationPublisherKind.ADMIN ->
            sessions.map { list -> list.filter { it.isActive } }.stateIn(scope, SharingStarted.Eagerly, emptyList())
        NotificationPublisherKind.TEACHER ->
            combine(sessions, teacherAssignments) { allSessions, assignments ->
                val allowed = assignments.map { it.sessionId }.toSet()
                allSessions.filter { it.isActive && it.sessionId in allowed }.sortedByDescending { it.startYear }
            }.stateIn(scope, SharingStarted.Eagerly, emptyList())
        NotificationPublisherKind.NONE -> MutableStateFlow(emptyList())
    }

    private val _publishAccess = MutableStateFlow(
        when {
            publisherKind == NotificationPublisherKind.NONE -> NotificationPublishAccess.DENIED
            permissionCheck == null -> NotificationPublishAccess.ALLOWED
            else -> NotificationPublishAccess.CHECKING
        },
    )
    val publishAccess: StateFlow<NotificationPublishAccess> = _publishAccess.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _busyActionId = MutableStateFlow<String?>(null)
    val busyActionId: StateFlow<String?> = _busyActionId.asStateFlow()

    private val _rowErrors = MutableStateFlow<Map<String, String>>(emptyMap())
    val rowErrors: StateFlow<Map<String, String>> = _rowErrors.asStateFlow()

    private val _composeError = MutableStateFlow<String?>(null)
    val composeError: StateFlow<String?> = _composeError.asStateFlow()

    private val _notice = MutableStateFlow<String?>(null)
    val notice: StateFlow<String?> = _notice.asStateFlow()

    init {
        if (permissionCheck != null) {
            launch {
                try {
                    _publishAccess.value = if (permissionCheck()) NotificationPublishAccess.ALLOWED else NotificationPublishAccess.DENIED
                } finally {
                    if (_publishAccess.value == NotificationPublishAccess.CHECKING) {
                        _publishAccess.value = NotificationPublishAccess.DENIED
                    }
                }
            }
        }
        _loading.value = false
    }

    fun refresh() = launch { refreshNow(context.value) }

    fun send(draft: NotificationDraft) = launch {
        try {
            _busyActionId.value = SEND_ACTION
            _composeError.value = null
            _notice.value = null

            require(_publishAccess.value == NotificationPublishAccess.ALLOWED) {
                "This account does not have permission to publish notifications."
            }
            require(accountKey.isNotBlank()) { "Your signed-in account could not be identified." }

            val title = draft.title.trim()
            val body = draft.body.trim()
            require(title.length in 3..120) { "Use a title between 3 and 120 characters." }
            require(body.length in 5..2000) { "Use a message between 5 and 2,000 characters." }
            require(draft.expiresAt == null || draft.expiresAt.isAfter(Instant.now())) {
                "The expiry date must be in the future."
            }

            val (targetRole, targetDepartment, targetSession) = when (publisherKind) {
                NotificationPublisherKind.ADMIN -> {
                    val role = draft.targetRole
                    val dept = draft.departmentId
                    val session = draft.sessionId
                    require(session == null || role == NotificationTargetRole.STUDENT) {
                        "Session notices can only target students."
                    }
                    require(dept == null || session == null) {
                        "Choose either a department or an academic session, not both."
                    }
                    require(dept == null || role != NotificationTargetRole.ADMIN) {
                        "Admin notices are always college-wide."
                    }
                    if (session != null) {
                        require(publishSessions.value.any { it.sessionId == session }) { "Choose a valid academic session." }
                    }
                    if (dept != null) {
                        require(departments.value.any { it.deptId == dept }) { "Choose a valid department." }
                    }
                    Triple(role, dept, session)
                }
                NotificationPublisherKind.TEACHER -> {
                    val session = draft.sessionId
                    require(session != null && publishSessions.value.any { it.sessionId == session }) {
                        "Choose one of your assigned sessions."
                    }
                    Triple(NotificationTargetRole.STUDENT, null, session)
                }
                NotificationPublisherKind.NONE -> throw IllegalStateException("Publishing is unavailable for this account.")
            }

            repository.send(title, body, targetRole, targetSession, accountKey, draft.priority, targetDepartment, draft.expiresAt)
            repository.syncAuthoredByCurrentUser(accountKey)
            _notice.value = "Notification sent to ${audienceLabel(targetRole, targetDepartment, targetSession)}."
        } catch (t: Throwable) {
            _composeError.value = t.userMessage("Could not send this notification.")
        } finally {
            _busyActionId.value = null
        }
    }

    fun delete(notification: Notification) = launch {
        try {
            _busyActionId.value = notification.notificationId
            _notice.value = null
            val canDelete = publisherKind == NotificationPublisherKind.ADMIN ||
                notification.createdByUid.equals(accountKey, ignoreCase = true)
            require(canDelete) { "Only the author or an Admin can delete this notification." }
            repository.delete(notification.notificationId)
            _rowErrors.value = _rowErrors.value - notification.notificationId
            _notice.value = "Notification deleted."
        } catch (t: Throwable) {
            _rowErrors.value = _rowErrors.value + (notification.notificationId to t.userMessage("Could not delete this notification."))
        } finally {
            _busyActionId.value = null
        }
    }

    fun clearComposeError() {
        _composeError.value = null
    }

    fun consumeNotice() {
        _notice.value = null
    }

    private suspend fun refreshNow(audience: NotificationAudienceContext) {
        if (viewerRole == NotificationTargetRole.STUDENT && audience.sessionId == null) {
            _loading.value = false
            return
        }
        _loading.value = true
        try {
            coroutineScope {
                val inboxRefresh = async { repository.sync(viewerRole, audience) }
                val historyRefresh = if (publisherKind != NotificationPublisherKind.NONE && accountKey.isNotBlank()) {
                    async { repository.syncAuthoredByCurrentUser(accountKey) }
                } else {
                    null
                }
                inboxRefresh.await()
                historyRefresh?.await()
            }
            repository.markViewedNow()
        } finally {
            _loading.value = false
        }
    }

    private fun audienceLabel(role: NotificationTargetRole, departmentId: String?, sessionId: String?): String {
        if (sessionId != null) {
            val session = sessions.value.firstOrNull { it.sessionId == sessionId }
            return if (session != null) {
                "the ${session.startYear}-${session.endYear} ${session.shift.name.lowercase(Locale.ROOT)} session"
            } else {
                sessionId
            }
        }
        if (departmentId != null) {
            return departments.value.firstOrNull { it.deptId == departmentId }?.name ?: departmentId
        }
        return when (role) {
            NotificationTargetRole.ALL -> "everyone"
            NotificationTargetRole.ADMIN -> "Admins"
            NotificationTargetRole.TEACHER -> "Teachers"
            NotificationTargetRole.STUDENT -> "Students"
        }
    }
}
