package com.mbd.cmsdesktop.ui.teacher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.awt.ComposeWindow
import com.mbd.cmscommon.domain.model.DailyAttendanceMark
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.AttendanceEditRequestRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.SessionAttendanceRepository
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import com.mbd.cmscommon.export.resolveRegisterContext
import com.mbd.cmscommon.ui.components.AttendanceHistoryWorkspace
import com.mbd.cmscommon.util.Outcome
import com.mbd.cmscommon.util.userMessageLogged
import com.mbd.cmscommon.export.attendanceRegisterExport
import com.mbd.cmsdesktop.platform.DocumentExporter
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Month-by-month attendance register for one session/course, reachable from [MarkAttendanceScreen]. */
@Composable
fun AttendanceHistoryScreen(
    sessionId: String,
    courseCode: String,
    initialMonth: YearMonth?,
    sessionRepository: AcademicSessionRepository,
    attendanceRepository: SessionAttendanceRepository,
    editRequestRepository: AttendanceEditRequestRepository,
    departmentRepository: DepartmentRepository,
    curriculumRepository: CurriculumRepository,
    timetableRepository: SessionTimetableRepository,
    window: ComposeWindow,
    onOpenStudent: (rollNumber: String, month: YearMonth) -> Unit,
) {
    val roster by sessionRepository.observeStudents(sessionId).collectAsState(initial = emptyList())
    var month by remember { mutableStateOf(initialMonth ?: YearMonth.now()) }
    var pendingCells by remember { mutableStateOf<Set<Pair<String, LocalDate>>>(emptySet()) }
    var requestState by remember { mutableStateOf<Outcome<Unit>?>(null) }
    val scope = rememberCoroutineScope()
    var marks by remember { mutableStateOf<Map<String, Map<LocalDate, DailyAttendanceMark>>>(emptyMap()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val session by sessionRepository.observeSession(sessionId).collectAsState(initial = null)
    val monthLabel = remember(month) { "${month.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)} ${month.year}" }


    LaunchedEffect(sessionId, courseCode, month) {
        loading = true
        error = null
        try {
            val from = month.atDay(1)
            val to = month.atEndOfMonth()
            val dailyMarks = attendanceRepository.marksBetween(sessionId, courseCode, from, to)
            marks = dailyMarks.groupBy { it.rollNumber }.mapValues { (_, ms) -> ms.associateBy { it.date } }
            pendingCells = runCatching { editRequestRepository.getPendingFor(sessionId, courseCode, from, to) }
                .getOrElse {
                    error = it.userMessageLogged("AttendanceHistoryScreen.loadPending", "Could not load pending edit requests.")
                    emptyList()
                }
                .map { it.rollNumber to it.date }
                .toSet()
        } catch (t: Throwable) {
            error = t.userMessageLogged("AttendanceHistoryScreen.load", "Could not load attendance history.")
        } finally {
            loading = false
        }
    }

    AttendanceHistoryWorkspace(
        courseCode = courseCode,
        month = month,
        monthLabel = monthLabel,
        loading = loading,
        roster = roster,
        marks = marks,
        pendingCells = pendingCells,
        requestState = requestState,
        onOpenStudent = { roll -> onOpenStudent(roll, month) },
        onSubmitEditRequest = { roll, date, current, status, late, reason ->
            if (requestState !is Outcome.Loading) {
                requestState = Outcome.Loading
                scope.launch {
                    requestState = try {
                        val semester = sessionRepository.observeSession(sessionId).first()?.currentSemester
                            ?: error("This session could not be found.")
                        editRequestRepository.submitRequest(
                            sessionId = sessionId,
                            semester = semester,
                            courseCode = courseCode,
                            date = date,
                            rollNumber = roll,
                            currentStatus = current?.status,
                            currentIsLate = current?.isLate,
                            requestedStatus = status,
                            requestedIsLate = late,
                            reason = reason,
                        )
                        pendingCells = pendingCells + (roll to date)
                        Outcome.Success(Unit)
                    } catch (t: Throwable) {
                        Outcome.Error(t.userMessageLogged("AttendanceHistoryScreen.submitEditRequest", "Could not send the edit request."), t)
                    }
                }
            }
        },
        onRequestStateConsumed = { requestState = null },
        onPreviousMonth = { month = month.minusMonths(1) },
        onNextMonth = { month = month.plusMonths(1) },
        onExport = { format ->
            scope.launch {
              try {
                val context = resolveRegisterContext(session, courseCode, departmentRepository, curriculumRepository, timetableRepository)
                DocumentExporter.export(window, attendanceRegisterExport(courseCode, session, month, roster, marks, context), format)
              } catch (t: Throwable) {
                error = t.userMessageLogged("AttendanceHistoryScreen.export", "Could not export the attendance register.")
              }
            }
        },
        errorMessage = error,
        onClearError = { error = null },
    )
}
