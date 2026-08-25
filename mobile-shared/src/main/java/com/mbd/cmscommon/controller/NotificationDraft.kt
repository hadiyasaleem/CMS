package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.NotificationPriority
import com.mbd.cmscommon.domain.model.NotificationTargetRole
import java.time.Instant

data class NotificationDraft(
    val title: String,
    val body: String,
    val targetRole: NotificationTargetRole,
    val priority: NotificationPriority,
    val departmentId: String? = null,
    val sessionId: String? = null,
    val expiresAt: Instant? = null,
)
