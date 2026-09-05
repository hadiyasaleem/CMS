package com.mbd.cmscommon.domain.model

import java.time.Duration
import java.time.Instant

data class PeopleHubSnapshot(
    val administratorCount: Int,
    val activeAdministratorCount: Int,
    val teacherCount: Int,
    val studentCount: Int,
    val delegatedTeacherCount: Int,
    val pendingLinkRequests: Int,
    val pendingMarkEdits: Int,
    val pendingExamReviews: Int,
    val repeatLinkRequests: Int,
    val oldestPendingDays: Long?,
) {
    val pendingReviews: Int get() = pendingLinkRequests + pendingMarkEdits + pendingExamReviews
    val inactiveAdministratorCount: Int get() = (administratorCount - activeAdministratorCount).coerceAtLeast(0)
}

fun peopleHubSnapshot(
    administrators: List<AdministratorAccount>,
    teachers: List<Teacher>,
    studentCount: Int,
    linkRequests: List<StudentLinkRequest>,
    markEditRequests: List<MarkEditRequest>,
    pendingExamReviews: Int = 0,
    now: Instant = Instant.now(),
): PeopleHubSnapshot {
    val uniqueAdministrators = administrators.distinctBy { it.id }
    val uniqueTeachers = teachers.distinctBy { it.teacherId }
    val pendingLinks = linkRequests.distinctBy { it.requestId }.filter { it.status == LinkRequestStatus.PENDING }
    val pendingEdits = markEditRequests.distinctBy { it.id }.filter { it.status == MarkEditStatus.PENDING }

    val oldest = (pendingLinks.map { it.createdAt } + pendingEdits.map { it.requestedAt }).minOrNull()

    val delegatedTeacherCount = uniqueTeachers.count {
        val p = it.permissions
        p.canApproveLinkRequests || p.canEditTimetable || p.canSendNotifications || p.canManageDatesheets
    }
    val repeatLinkRequests = pendingLinks.count { it.attemptCount > 1 }

    return PeopleHubSnapshot(
        administratorCount = uniqueAdministrators.size,
        activeAdministratorCount = uniqueAdministrators.count { it.status.equals("ACTIVE", ignoreCase = true) },
        teacherCount = uniqueTeachers.size,
        studentCount = studentCount.coerceAtLeast(0),
        delegatedTeacherCount = delegatedTeacherCount,
        pendingLinkRequests = pendingLinks.size,
        pendingMarkEdits = pendingEdits.size,
        pendingExamReviews = pendingExamReviews.coerceAtLeast(0),
        repeatLinkRequests = repeatLinkRequests,
        oldestPendingDays = oldest?.let { Duration.between(it, now).toDays().coerceAtLeast(0) },
    )
}
