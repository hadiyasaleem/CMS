package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.PeriodType
import com.mbd.cmscommon.domain.model.SemesterSubject
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import java.time.DayOfWeek
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

class SessionTimetableController(
    val sessionId: String,
    private val timetableRepository: SessionTimetableRepository,
    sessionRepository: AcademicSessionRepository,
    curriculumRepository: CurriculumRepository,
    teacherRepository: TeacherRepository,
    scope: CoroutineScope,
) : ScreenController(scope) {

    val session: StateFlow<AcademicSession?> =
        sessionRepository.observeSession(sessionId).stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

    val periods: StateFlow<List<SessionPeriod>> =
        timetableRepository.observeWeek(sessionId).stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subjects: StateFlow<List<SemesterSubject>> = session
        .flatMapLatest { s -> if (s == null) flowOf(emptyList()) else curriculumRepository.observeSemesterSubjects(s.sessionId, s.currentSemester) }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val teachers: StateFlow<List<Teacher>> =
        teacherRepository.observeActiveTeachers().stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        launch { timetableRepository.syncSession(sessionId) }
    }

    fun savePeriod(
        day: DayOfWeek,
        start: String,
        end: String,
        subject: SemesterSubject?,
        teacher: Teacher?,
        periodType: PeriodType,
        roomNo: String?,
        building: String?,
        notes: String?,
        effectiveFrom: LocalDate?,
        effectiveTo: LocalDate?,
        replaces: SessionPeriod?,
    ) = launch {
        require(periodType == PeriodType.BREAK || subject != null) { "Choose a subject for this period." }

        val normalizedStart = start.trim()
        val normalizedEnd = end.trim()
        val period = SessionPeriod(
            id = SessionPeriod.buildId(sessionId, day, normalizedStart),
            sessionId = sessionId,
            day = day,
            startTime = normalizedStart,
            endTime = normalizedEnd,
            courseCode = subject?.courseCode ?: "BREAK",
            subjectName = subject?.name ?: "Break",
            teacherId = if (periodType != PeriodType.BREAK) teacher?.teacherId ?: "" else "",
            teacherName = if (periodType != PeriodType.BREAK) teacher?.name ?: "" else "",
            periodType = periodType,
            creditHours = subject?.creditHours,
            roomNo = roomNo?.trim()?.takeIf { it.isNotBlank() },
            building = building?.trim()?.takeIf { it.isNotBlank() },
            notes = notes?.trim()?.takeIf { it.isNotBlank() },
            effectiveFrom = effectiveFrom,
            effectiveTo = effectiveTo,
        )

        validateTimetablePeriod(period, replaces, periods.value)?.let { throw IllegalArgumentException(it) }

        timetableRepository.savePeriod(period)
        if (replaces != null && (replaces.day != period.day || replaces.startTime != period.startTime)) {
            timetableRepository.removePeriod(replaces)
        }
    }

    fun removePeriod(period: SessionPeriod) = launch {
        timetableRepository.removePeriod(period)
    }
}
