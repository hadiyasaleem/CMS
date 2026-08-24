package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.domain.model.TeacherStatus
import com.mbd.cmscommon.teacher.ResolvedAssignment
import java.util.Locale

data class TeacherDirectorySnapshot(
    val entries: List<TeacherDirectoryEntry>,
    val totalAssignments: Int,
    val activeCount: Int,
    val assignedCount: Int,
    val delegatedCount: Int,
    val incompleteCount: Int,
)

fun teacherDirectorySnapshot(
    teachers: List<Teacher>,
    assignments: Map<String, List<ResolvedAssignment>>,
): TeacherDirectorySnapshot {
    val normalizedAssignments = assignments.entries
        .groupBy({ it.key.trim().lowercase(Locale.ROOT) }, { it.value })
        .mapValues { (_, groups) ->
            groups.flatten()
                .distinctBy { it.sessionId to it.courseCode.uppercase(Locale.ROOT) }
                .sortedWith(compareBy({ it.sessionLabel }, { it.courseCode }))
        }

    val cleanedTeachers = teachers.map { teacher ->
        teacher.copy(
            name = teacher.name.trim(),
            email = teacher.email.trim(),
            phone = teacher.phone?.trim()?.takeIf { it.isNotBlank() },
            deptId = teacher.deptId?.trim()?.takeIf { it.isNotBlank() },
            designation = teacher.designation?.trim()?.takeIf { it.isNotBlank() },
            qualification = teacher.qualification?.trim()?.takeIf { it.isNotBlank() },
            specialization = teacher.specialization?.trim()?.takeIf { it.isNotBlank() },
            officeRoom = teacher.officeRoom?.trim()?.takeIf { it.isNotBlank() },
            gender = teacher.gender?.trim()?.uppercase(Locale.ROOT)?.takeIf { it.isNotBlank() },
        )
    }

    val uniqueTeachers = cleanedTeachers
        .distinctBy { it.email.lowercase(Locale.ROOT).ifBlank { it.teacherId.trim().lowercase(Locale.ROOT) } }
        .sortedBy { it.name.lowercase(Locale.ROOT) }

    val entries = uniqueTeachers.map { teacher ->
        val keys = listOf(teacher.teacherId, teacher.email).map { it.trim().lowercase(Locale.ROOT) }.distinct()
        val resolved = keys.flatMap { normalizedAssignments[it].orEmpty() }
            .distinctBy { it.sessionId to it.courseCode.uppercase(Locale.ROOT) }
        TeacherDirectoryEntry(teacher, resolved, teacherProfileCompleteness(teacher), teacherPermissionCount(teacher))
    }

    return TeacherDirectorySnapshot(
        entries = entries,
        totalAssignments = entries.sumOf { it.assignments.size },
        activeCount = entries.count { it.teacher.status == TeacherStatus.ACTIVE },
        assignedCount = entries.count { it.assignments.isNotEmpty() },
        delegatedCount = entries.count { it.permissionCount > 0 },
        incompleteCount = entries.count { it.profileCompleteness < 100 },
    )
}

fun teacherProfileCompleteness(teacher: Teacher): Int {
    val checks = listOf(
        !teacher.deptId.isNullOrBlank(),
        !teacher.designation.isNullOrBlank(),
        (teacher.phone?.count { it.isDigit() } ?: 0) >= 10,
        !teacher.qualification.isNullOrBlank(),
        !teacher.specialization.isNullOrBlank(),
        !teacher.officeRoom.isNullOrBlank(),
        teacher.gender?.uppercase(Locale.ROOT) in setOf("MALE", "FEMALE", "OTHER"),
    )
    return checks.count { it } * 100 / checks.size
}

fun teacherPermissionCount(teacher: Teacher): Int {
    val permissions = teacher.permissions
    return listOf(
        permissions.canApproveLinkRequests,
        permissions.canEditTimetable,
        permissions.canSendNotifications,
        permissions.canManageDatesheets,
    ).count { it }
}
