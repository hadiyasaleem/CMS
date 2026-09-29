package com.mbd.cmscommon.domain.model

data class AttendanceRegisterSummary(
    val total: Int,
    val marked: Int,
    val present: Int,
    val absent: Int,
    val leave: Int,
    val late: Int,
    val atRisk: Int,
) {
    val unmarked: Int get() = (total - marked).coerceAtLeast(0)
    val progress: Float get() = if (total == 0) 0f else marked.toFloat() / total
}

fun attendanceRegisterSummary(
    roster: List<SessionStudent>,
    statuses: Map<String, AttendanceStatus>,
    lateRolls: Set<String>,
    termPercents: Map<String, Float>,
): AttendanceRegisterSummary {
    // Keyed by SessionStudent.id ("${sessionId}_$rollNumber"), never bare roll numbers -- a merged class's two
    // sessions may otherwise reuse the same roll number and silently collide.
    val ids = roster.map { it.id }.toSet()
    val validStatuses = statuses.filterKeys { it in ids }

    return AttendanceRegisterSummary(
        total = roster.size,
        marked = validStatuses.size,
        present = validStatuses.values.count { it == AttendanceStatus.PRESENT },
        absent = validStatuses.values.count { it == AttendanceStatus.ABSENT },
        leave = validStatuses.values.count { it == AttendanceStatus.LEAVE },
        late = lateRolls.count { it in ids },
        atRisk = termPercents.count { (id, percent) -> id in ids && percent < 65f },
    )
}
