package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.SemesterTerm
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.domain.model.StudentTermAttendance
import com.mbd.cmscommon.domain.model.studentTermAttendance
import com.mbd.cmscommon.domain.model.termMonths
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.SessionAttendanceRepository
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

/** One student's attendance in one subject across the current term, month by month. */
class StudentAttendanceSummaryController(
    val sessionId: String,
    val courseCode: String,
    val rollNumber: String,
    private val sessionRepository: AcademicSessionRepository,
    private val curriculumRepository: CurriculumRepository,
    private val attendanceRepository: SessionAttendanceRepository,
    scope: CoroutineScope,
    private val today: () -> LocalDate = LocalDate::now,
) : ScreenController(scope) {

    private val _student = MutableStateFlow<SessionStudent?>(null)
    val student: StateFlow<SessionStudent?> = _student.asStateFlow()

    private val _session = MutableStateFlow<AcademicSession?>(null)
    val session: StateFlow<AcademicSession?> = _session.asStateFlow()

    private val _term = MutableStateFlow<SemesterTerm?>(null)
    val term: StateFlow<SemesterTerm?> = _term.asStateFlow()

    private val _summary = MutableStateFlow<StudentTermAttendance?>(null)
    val summary: StateFlow<StudentTermAttendance?> = _summary.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    init {
        refresh()
    }

    fun refresh() = launch {
        _loading.value = true
        try {
            _student.value = sessionRepository.observeStudents(sessionId).first()
                .firstOrNull { it.rollNumber.equals(rollNumber, ignoreCase = true) }
            val session = sessionRepository.observeSession(sessionId).first()
            _session.value = session
            val term = session?.let { curriculumRepository.getSemesterTerm(sessionId, it.currentSemester) }
            _term.value = term

            val now = today()
            // Without term dates, look back a year and keep only months that have marks.
            val from = term?.startDate ?: now.minusYears(1).withDayOfMonth(1)
            val to = listOfNotNull(term?.endDate, now).min()
            val marks = if (to < from) emptyList() else {
                attendanceRepository.marksBetween(sessionId, courseCode, from, to)
                    .filter { it.rollNumber.equals(rollNumber, ignoreCase = true) }
            }
            _summary.value = studentTermAttendance(marks, termMonths(term?.startDate, term?.endDate, now, marks))
        } finally {
            _loading.value = false
        }
    }
}
