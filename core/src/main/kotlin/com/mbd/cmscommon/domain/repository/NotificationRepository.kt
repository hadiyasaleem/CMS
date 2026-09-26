package com.mbd.cmscommon.domain.repository

import com.mbd.cmscommon.domain.model.AudienceViewer
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.TaughtClass
import com.mbd.cmscommon.domain.model.audienceReaches
import com.mbd.cmscommon.domain.model.audienceTarget
import com.mbd.cmscommon.domain.model.Notification
import com.mbd.cmscommon.domain.model.NotificationPriority
import com.mbd.cmscommon.domain.model.NotificationTargetRole
import java.time.Instant
import kotlinx.coroutines.flow.Flow

/**
 * Who is reading notifications. A student gives their session, department and shift; a teacher gives their home
 * department and [taughtClasses] (the sessions and shifts they teach). Mirrors the server's RLS rule offline.
 */
data class NotificationAudienceContext(
    val sessionId: String? = null,
    val departmentId: String? = null,
    val shift: Session? = null,
    val taughtClasses: Set<TaughtClass>? = null,
) {
    fun viewerFor(role: NotificationTargetRole): AudienceViewer = when (role) {
        NotificationTargetRole.STUDENT -> AudienceViewer.Student(departmentId, sessionId, shift)
        NotificationTargetRole.TEACHER -> AudienceViewer.Teacher(departmentId, taughtClasses.orEmpty())
        NotificationTargetRole.ADMIN, NotificationTargetRole.ALL -> AudienceViewer.Admin
    }
}

/** Whether a cached notification is for this reader: their role (or ALL) and a target that reaches them. */
fun notificationReaches(notification: Notification, role: NotificationTargetRole, context: NotificationAudienceContext): Boolean {
    val roleMatches = notification.targetRole == null || notification.targetRole == role || notification.targetRole == NotificationTargetRole.ALL
    return roleMatches && audienceReaches(notification.audienceTarget, context.viewerFor(role))
}

interface NotificationRepository {
    fun observeForRole(role: NotificationTargetRole, context: NotificationAudienceContext = NotificationAudienceContext()): Flow<List<Notification>>
    fun observeAuthoredByCurrentUser(uid: String): Flow<List<Notification>>
    fun observeUnreadCount(role: NotificationTargetRole, context: NotificationAudienceContext = NotificationAudienceContext()): Flow<Int>

    suspend fun sync(role: NotificationTargetRole, context: NotificationAudienceContext = NotificationAudienceContext())
    suspend fun syncAuthoredByCurrentUser(uid: String)

    suspend fun send(
        title: String,
        body: String,
        targetRole: NotificationTargetRole,
        targetOfferingId: String?,
        createdByUid: String,
        priority: NotificationPriority = NotificationPriority.NORMAL,
        targetDeptId: String? = null,
        expiresAt: Instant? = null,
        /** Narrows a session-targeted notice to one shift. */
        targetShift: Session? = null,
    )

    suspend fun delete(notificationId: String)
    suspend fun markViewedNow()
}
