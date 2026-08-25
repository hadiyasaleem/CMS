package com.mbd.cmscommon.domain.repository

import com.mbd.cmscommon.domain.model.Notification
import com.mbd.cmscommon.domain.model.NotificationPriority
import com.mbd.cmscommon.domain.model.NotificationTargetRole
import java.time.Instant
import kotlinx.coroutines.flow.Flow

data class NotificationAudienceContext(
    val sessionId: String? = null,
    val departmentId: String? = null,
)

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
    )

    suspend fun delete(notificationId: String)
    suspend fun markViewedNow()
}
