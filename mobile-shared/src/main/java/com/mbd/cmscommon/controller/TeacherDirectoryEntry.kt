package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.teacher.ResolvedAssignment

data class TeacherDirectoryEntry(
    val teacher: Teacher,
    val assignments: List<ResolvedAssignment>,
    val profileCompleteness: Int,
    val permissionCount: Int,
) {
    val sessionCount: Int get() = assignments.distinctBy { it.sessionId }.size
}
