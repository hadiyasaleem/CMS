package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.teacher.ResolvedAssignment

data class TeacherDirectoryEntry(
    val teacher: Teacher,
    val assignments: List<ResolvedAssignment>,
    val profileCompleteness: Int,
    val permissionCount: Int,
) {
    // A merged lecture's linked sessions count too, not just each assignment's own primary session.
    val sessionCount: Int get() = assignments.flatMap { it.sessionIds }.distinct().size
}
