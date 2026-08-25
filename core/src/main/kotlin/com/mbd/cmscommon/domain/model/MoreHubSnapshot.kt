package com.mbd.cmscommon.domain.model

import java.time.Instant

enum class MoreSummarySource {
    ADMINISTRATORS,
    AUTHORED_NOTIFICATIONS,
    UNREAD_NOTIFICATIONS,
}

data class MoreHubSnapshot(
    val accountEmail: String,
    val accountStatus: String,
    val lastLoginAt: Instant?,
    val administratorCount: Int,
    val authoredNotifications: Int,
    val urgentAuthoredNotifications: Int,
    val unreadNotifications: Int,
    val unavailableSources: Set<MoreSummarySource> = emptySet(),
)

fun moreHubSnapshot(
    accountKey: String,
    administrators: List<AdministratorAccount>,
    authoredNotifications: List<Notification>,
    unreadNotifications: Int,
    unavailableSources: Set<MoreSummarySource> = emptySet(),
): MoreHubSnapshot {
    val distinctAdministrators = administrators.distinctBy { it.id }
    val account = distinctAdministrators.firstOrNull { it.email.equals(accountKey, ignoreCase = true) }
    val distinctNotifications = authoredNotifications.distinctBy { it.notificationId }

    return MoreHubSnapshot(
        accountEmail = account?.email ?: accountKey,
        accountStatus = account?.status?.takeIf { it.isNotBlank() } ?: "UNKNOWN",
        lastLoginAt = account?.lastLoginAt,
        administratorCount = distinctAdministrators.size,
        authoredNotifications = distinctNotifications.size,
        urgentAuthoredNotifications = distinctNotifications.count { it.priority == NotificationPriority.URGENT },
        unreadNotifications = unreadNotifications.coerceAtLeast(0),
        unavailableSources = unavailableSources,
    )
}
