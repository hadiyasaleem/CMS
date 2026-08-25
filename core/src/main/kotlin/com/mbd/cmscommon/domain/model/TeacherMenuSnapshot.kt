package com.mbd.cmscommon.domain.model

import com.mbd.cmscommon.teacher.ResolvedAssignment

data class TeacherMenuSnapshot(
    val teacherName: String,
    val assignmentCount: Int,
    val sessionCount: Int,
    val unreadNotifications: Int,
    val pendingLinkRequests: Int,
    val profileCompleteness: Int,
    val canApproveLinkRequests: Boolean,
    val canSendNotifications: Boolean,
)

fun teacherMenuSnapshot(
    profile: Teacher?,
    assignments: List<ResolvedAssignment>,
    unreadNotifications: Int,
    pendingLinkRequests: Int,
): TeacherMenuSnapshot {
    val profileSummary = teacherProfileSnapshot(profile, assignments)
    val canApprove = profile?.permissions?.canApproveLinkRequests == true
    val name = profile?.name?.trim()?.takeIf { it.isNotBlank() } ?: "Teacher"

    return TeacherMenuSnapshot(
        teacherName = name,
        assignmentCount = profileSummary.assignmentCount,
        sessionCount = profileSummary.sessionCount,
        unreadNotifications = unreadNotifications.coerceAtLeast(0),
        pendingLinkRequests = if (canApprove) pendingLinkRequests.coerceAtLeast(0) else 0,
        profileCompleteness = profileSummary.profileCompleteness,
        canApproveLinkRequests = canApprove,
        canSendNotifications = profile?.permissions?.canSendNotifications == true,
    )
}
