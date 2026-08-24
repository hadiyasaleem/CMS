package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.PeriodType
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.SessionAttendanceRepository
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale
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
    ) { tallies, periods ->
        val today = LocalDate.now()
        val activeToday = activeLectures(periods, today)

        val total = tallies.sumOf { it.total }
        val present = tallies.sumOf { it.present }
        val overall = if (total == 0) 0f else (present * 100f) / total

        val weakest = tallies.filter { it.total > 0 }.minByOrNull { it.percentage }
            ?.takeIf { it.percentage < 75f }
            ?.let { WeakSubject(it.courseCode, it.percentage) }

        StudentHomeUi(
            overallPercent = overall,
            subjectCount = tallies.count { it.total > 0 },
            lecturesToday = activeToday.count { it.day == today.dayOfWeek },
            nextClass = nextClassFrom(periods, today),
            weakestSubject = weakest,
        )
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), StudentHomeUi())

    fun refresh() = launch {
        runCatching { attendanceRepository.syncSession(sessionId) }
        runCatching { timetableRepository.syncSession(sessionId) }
    }
}

fun activeLectures(periods: List<SessionPeriod>, date: LocalDate): List<SessionPeriod> =
    periods.filter {
        it.periodType == PeriodType.LECTURE &&
            (it.effectiveFrom == null || !date.isBefore(it.effectiveFrom)) &&
            (it.effectiveTo == null || !date.isAfter(it.effectiveTo))
    }

fun nextClassFrom(periods: List<SessionPeriod>, date: LocalDate = LocalDate.now(), time: LocalTime = LocalTime.now()): NextClass? {
    if (periods.isEmpty()) return null
    for (offset in 0 until 7) {
        val day = date.plusDays(offset.toLong())
        val dayOfWeek = date.dayOfWeek.plus(offset.toLong())
        val candidate = activeLectures(periods, day)
            .filter { it.day == dayOfWeek }
            .sortedBy { it.startTime }
            .firstOrNull { period ->
                val start = runCatching { LocalTime.parse(period.startTime) }.getOrNull() ?: return@firstOrNull false
                offset != 0 || start.isAfter(time)
            } ?: continue

        val dayLabel = if (offset == 0) {
            "Today"
        } else {
            dayOfWeek.name.lowercase(Locale.ROOT).replaceFirstChar { it.uppercase(Locale.ROOT) }
        }
        val location = listOfNotNull(
            candidate.roomNo?.takeIf { it.isNotBlank() },
            candidate.building?.takeIf { it.isNotBlank() },
        ).joinToString(" / ").takeIf { it.isNotBlank() }

        return NextClass(candidate.courseCode, candidate.subjectName, candidate.timeRange, candidate.teacherName, dayLabel, location)
    }
    return null
}
