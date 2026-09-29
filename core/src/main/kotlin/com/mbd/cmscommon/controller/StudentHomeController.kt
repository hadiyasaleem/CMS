package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.PeriodType
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.SessionAttendanceRepository
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import com.mbd.cmscommon.util.orLogCritical
import com.mbd.cmscommon.util.parseClock
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class StudentHomeController(
    private val sessionId: String,
    rollNumber: String,
    sessionRepository: AcademicSessionRepository,
    private val attendanceRepository: SessionAttendanceRepository,
    private val timetableRepository: SessionTimetableRepository,
    scope: CoroutineScope,
) : ScreenController(scope) {

    val session: StateFlow<AcademicSession?> =
        sessionRepository.observeSession(sessionId).stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

    val me: StateFlow<SessionStudent?> = sessionRepository.observeStudents(sessionId)
        .map { list -> list.firstOrNull { it.rollNumber == rollNumber } }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

    val ui: StateFlow<StudentHomeUi> = combine(
        attendanceRepository.observeStudentTallies(sessionId, rollNumber),
        timetableRepository.observeWeek(sessionId),
        me,
    ) { tallies, allPeriods, student ->
        // Only the student's own shift's grid: Morning and Evening periods can share slot times.
        val periods = periodsForShift(allPeriods, student?.shift)
        val today = LocalDate.now()
        val todaysClasses = activeLectures(periods, today)
            .filter { it.day == today.dayOfWeek }
            .sortedBy { parseClock(it.startTime) ?: LocalTime.MAX }
        val now = LocalTime.now()
        val nextClassId = todaysClasses.firstOrNull { period ->
            val start = parseClock(period.startTime)
            start != null && !start.isBefore(now)
        }?.id

        val total = tallies.sumOf { it.total }
        val present = tallies.sumOf { it.present }
        val overall = if (total == 0) 0f else (present * 100f) / total

        val weakest = tallies.filter { it.total > 0 }.minByOrNull { it.percentage }
            ?.takeIf { it.percentage < 75f }
            ?.let { WeakSubject(it.courseCode, it.percentage) }

        StudentHomeUi(
            overallPercent = overall,
            subjectCount = tallies.count { it.total > 0 },
            todaysClasses = todaysClasses,
            nextClassId = nextClassId,
            weakestSubject = weakest,
        )
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), StudentHomeUi())

    fun refresh() = launch {
        runCatching { attendanceRepository.syncSession(sessionId) }.orLogCritical("StudentHomeController.refresh.attendance")
        runCatching { timetableRepository.syncSession(sessionId) }.orLogCritical("StudentHomeController.refresh.timetable")
    }
}

fun activeLectures(periods: List<SessionPeriod>, date: LocalDate): List<SessionPeriod> =
    periods.filter {
        it.periodType == PeriodType.LECTURE &&
            (it.effectiveFrom == null || !date.isBefore(it.effectiveFrom)) &&
            (it.effectiveTo == null || !date.isAfter(it.effectiveTo))
    }
