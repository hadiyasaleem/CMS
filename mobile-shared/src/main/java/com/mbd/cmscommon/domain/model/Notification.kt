package com.mbd.cmscommon.domain.model

import java.time.Instant

enum class NotificationPriority {
    NORMAL,
    IMPORTANT,
    URGENT,
}

enum class NotificationTargetRole {
    ADMIN,
    TEACHER,
    STUDENT,
    ALL,
}

data class Notification(
    val notificationId: String,
    val title: String,
    val body: String,
    val targetRole: NotificationTargetRole?,
    val targetOfferingId: String? = null,
    val createdByUid: String,
    val priority: NotificationPriority = NotificationPriority.NORMAL,
    val targetDeptId: String? = null,
    val attachmentPath: String? = null,
    val expiresAt: Instant? = null,
    override val entityId: Long = 0L,
    override val createdAt: Instant,
    override val createdBy: String? = null,
    override val updatedAt: Instant = Instant.EPOCH,
    override val updatedBy: String? = null,
) : BaseEntity()

fun canonicalNotifications(items: List<Notification>): List<Notification> =
    items.distinctBy { it.notificationId }

fun reviewReasons(
    notification: Notification,
    validDepartmentIds: Set<String> = emptySet(),
    validSessionIds: Set<String> = emptySet(),
    now: Instant = Instant.now(),
): List<String> {
    val reasons = mutableListOf<String>()
    if (notification.notificationId.isBlank()) reasons += "Missing notification ID"
    if (notification.title.isBlank()) reasons += "Missing title"
    if (notification.body.isBlank()) reasons += "Missing message"
    if (notification.targetRole == null) reasons += "Missing target role"
    if (notification.createdByUid.isBlank()) reasons += "Missing publisher"
    if (notification.createdAt == Instant.EPOCH) reasons += "Missing publish time"
    if (notification.createdAt.isAfter(now.plusSeconds(300))) reasons += "Publish time is in the future"

    if (!notification.targetOfferingId.isNullOrBlank() && notification.targetRole != NotificationTargetRole.STUDENT) {
        reasons += "Session target is only valid for students"
    }
    if (!notification.targetDeptId.isNullOrBlank() && notification.targetRole == NotificationTargetRole.ADMIN) {
        reasons += "Admin notices must be college-wide"
    }
    if (!notification.targetDeptId.isNullOrBlank() && validDepartmentIds.isNotEmpty() &&
        notification.targetDeptId !in validDepartmentIds
    ) {
        reasons += "Department is inactive or missing"
    }
    if (!notification.targetOfferingId.isNullOrBlank() && validSessionIds.isNotEmpty() &&
        notification.targetOfferingId !in validSessionIds
    ) {
        reasons += "Session is inactive or missing"
    }
    if (notification.expiresAt != null && notification.createdAt != Instant.EPOCH &&
        !notification.expiresAt.isAfter(notification.createdAt)
    ) {
        reasons += "Expiry is not after publish time"
    }
    return reasons
}
