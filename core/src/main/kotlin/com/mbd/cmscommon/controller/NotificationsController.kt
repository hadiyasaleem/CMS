package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.util.CmsException
import com.mbd.cmscommon.util.requireValid
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.Notification
import com.mbd.cmscommon.domain.model.NotificationTargetRole
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.NotificationAudienceContext
import com.mbd.cmscommon.domain.repository.NotificationRepository
import com.mbd.cmscommon.teacher.ResolvedAssignment
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
    val publisherKind: NotificationPublisherKind = NotificationPublisherKind.NONE,
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
                // A merged lecture's linked sessions are also ones this teacher may notify, not just the primary.
                val allowed = assignments.flatMap { it.sessionIds }.toSet()
                allSessions.filter { it.isActive && it.sessionId in allowed }.sortedByDescending { it.startYear }
            }.stateIn(scope, SharingStarted.Eagerly, emptyList())
        NotificationPublisherKind.NONE -> MutableStateFlow(emptyList())
    }

    /** The shifts a teacher teaches in each session; a teacher may narrow a notice only to one of those. */
    val teachingShifts: StateFlow<Map<String, Set<Session>>> = teacherAssignments
        .map { list ->
            list.filter { it.classShift != null }
                .flatMap { a -> a.sessionIds.map { sid -> sid to a.classShift!! } }
                .groupBy({ it.first }, { it.second })
                .mapValues { (_, shifts) -> shifts.toSet() }
        }
        .stateIn(scope, SharingStarted.Eagerly, emptyMap())

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
            launch("check your permission to send notifications") {
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

    fun refresh() = launch("refresh notifications") { refreshNow(context.value) }

    fun send(draft: NotificationDraft) = launch("send the notification") {
        try {
            _busyActionId.value = SEND_ACTION
            _composeError.value = null
            _notice.value = null

            requireValid(_publishAccess.value == NotificationPublishAccess.ALLOWED) {
                "This account does not have permission to publish notifications."
            }
            requireValid(accountKey.isNotBlank()) { "Your signed-in account could not be identified." }

            val title = draft.title.trim()
            val body = draft.body.trim()
            requireValid(title.length in 3..120) { "Use a title between 3 and 120 characters." }
            requireValid(body.length in 5..2000) { "Use a message between 5 and 2,000 characters." }
            requireValid(draft.expiresAt == null || draft.expiresAt.isAfter(Instant.now())) {
                "The expiry date must be in the future."
            }

            // Department -> Session -> Shift targeting: each level is optional and narrows the audience.
            val sessionPicked = draft.sessionId?.let { id -> publishSessions.value.firstOrNull { it.sessionId == id } }
            requireValid(draft.sessionId == null || sessionPicked != null) {
                if (publisherKind == NotificationPublisherKind.TEACHER) "Choose one of your assigned sessions." else "Choose a valid academic session."
            }
            requireValid(draft.shift == null || sessionPicked != null) { "Choose a session before narrowing to a shift." }
            requireValid(draft.shift == null || sessionPicked!!.runs(draft.shift)) { "This session does not run the ${draft.shift?.label} shift." }
            val targetDepartment = sessionPicked?.deptId ?: draft.departmentId
            requireValid(draft.departmentId == null || targetDepartment == draft.departmentId) {
                "The session does not belong to the chosen department."
            }

            val targetRole = when (publisherKind) {
                NotificationPublisherKind.ADMIN -> {
                    requireValid(targetDepartment == null || draft.targetRole != NotificationTargetRole.ADMIN) {
                        "Admin notices are always college-wide."
                    }
                    if (targetDepartment != null) {
                        requireValid(departments.value.any { it.deptId == targetDepartment }) { "Choose a valid department." }
                    }
                    draft.targetRole
                }
                NotificationPublisherKind.TEACHER -> {
                    requireValid(sessionPicked != null) { "Choose one of your assigned sessions." }
                    requireValid(draft.shift == null || draft.shift in teachingShifts.value[sessionPicked!!.sessionId].orEmpty()) {
                        "You don't teach the ${draft.shift?.label} shift of this session."
                    }
                    NotificationTargetRole.STUDENT
                }
                NotificationPublisherKind.NONE -> throw CmsException.Permission("Publishing is unavailable for this account.")
            }

            repository.send(title, body, targetRole, sessionPicked?.sessionId, accountKey, draft.priority, targetDepartment, draft.expiresAt, draft.shift)
            repository.syncAuthoredByCurrentUser(accountKey)
            _notice.value = "Notification sent to ${audienceLabel(targetRole, targetDepartment, sessionPicked?.sessionId, draft.shift)}."
        } catch (t: Throwable) {
            _composeError.value = t.userMessageLogged("Could not send this notification.")
        } finally {
            _busyActionId.value = null
        }
    }

    fun delete(notification: Notification) = launch("delete the notification") {
        try {
            _busyActionId.value = notification.notificationId
            _notice.value = null
            val canDelete = publisherKind == NotificationPublisherKind.ADMIN ||
                notification.createdByUid.equals(accountKey, ignoreCase = true)
            requireValid(canDelete) { "Only the author or an Admin can delete this notification." }
            repository.delete(notification.notificationId)
            _rowErrors.value = _rowErrors.value - notification.notificationId
            _notice.value = "Notification deleted."
        } catch (t: Throwable) {
            _rowErrors.value = _rowErrors.value + (notification.notificationId to t.userMessageLogged("Could not delete this notification."))
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

    private fun audienceLabel(role: NotificationTargetRole, departmentId: String?, sessionId: String?, shift: Session?): String {
        if (sessionId != null) {
            val session = sessions.value.firstOrNull { it.sessionId == sessionId }
            val shiftPart = shift?.let { " (${it.label} shift)" }.orEmpty()
            return if (session != null) {
                "the ${session.startYear}-${session.endYear} session$shiftPart"
            } else {
                sessionId + shiftPart
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
