package com.mbd.cmscommon.domain.model

import com.mbd.cmscommon.teacher.ResolvedAssignment

data class TeacherProfileSnapshot(
    val profileCompleteness: Int,
    val assignmentCount: Int,
    val sessionCount: Int,
    val subjectCount: Int,
    val grantedPermissionCount: Int,
)

fun teacherProfileSnapshot(profile: Teacher?, assignments: List<ResolvedAssignment>): TeacherProfileSnapshot {
    val profileValues = profile?.let {
        listOf(it.deptId, it.designation, it.phone, it.qualification, it.specialization, it.officeRoom, it.gender)
    } ?: emptyList()
    val completeness = if (profileValues.isEmpty()) {
        0
    } else {
        (profileValues.count { !it.isNullOrBlank() } * 100) / profileValues.size
    }

    val distinctAssignments = assignments.distinctBy {
        it.sessionId.trim().lowercase() to it.courseCode.trim().lowercase()
    }
    val sessionCount = assignments.map { it.sessionId.trim().lowercase() }.filter { it.isNotBlank() }.distinct().size
    val subjectCount = assignments.map { it.courseCode.trim().lowercase() }.filter { it.isNotBlank() }.distinct().size

    val permissions = profile?.permissions
    val grantedPermissionCount = listOf(
        permissions?.canApproveLinkRequests == true,
        permissions?.canEditTimetable == true,
        permissions?.canSendNotifications == true,
        permissions?.canManageDatesheets == true,
    ).count { it }

    return TeacherProfileSnapshot(completeness, distinctAssignments.size, sessionCount, subjectCount, grantedPermissionCount)
}
