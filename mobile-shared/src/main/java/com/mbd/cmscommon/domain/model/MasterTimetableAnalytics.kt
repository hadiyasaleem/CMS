package com.mbd.cmscommon.domain.model

data class MasterTimetableSummary(
    val sessionCount: Int,
    val scheduledSessionCount: Int,
    val periodCount: Int,
    val timeSlotCount: Int,
    val breakCount: Int,
    val unassignedTeacherCount: Int,
    val missingRoomCount: Int,
) {
    val coveragePercentage: Int? get() =
        if (sessionCount == 0) null else (scheduledSessionCount * 100) / sessionCount
}

fun masterTimetableSummary(sessions: List<AcademicSession>, periods: List<SessionPeriod>): MasterTimetableSummary {
    val sessionIds = sessions.map { it.sessionId }.toSet()
    val visiblePeriods = periods.filter { it.sessionId in sessionIds }
    val teachingPeriods = visiblePeriods.filter { it.periodType != PeriodType.BREAK }

    return MasterTimetableSummary(
        sessionCount = sessions.size,
        scheduledSessionCount = visiblePeriods.map { it.sessionId }.distinct().size,
        periodCount = teachingPeriods.size,
        timeSlotCount = visiblePeriods.map { it.timeRange }.distinct().size,
        breakCount = visiblePeriods.count { it.periodType == PeriodType.BREAK },
        unassignedTeacherCount = teachingPeriods.count { it.teacherId.isBlank() },
        missingRoomCount = teachingPeriods.count { it.roomNo.isNullOrBlank() },
    )
}
