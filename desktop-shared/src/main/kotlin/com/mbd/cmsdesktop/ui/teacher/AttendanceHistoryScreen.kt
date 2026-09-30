package com.mbd.cmsdesktop.ui.teacher

import com.mbd.cmscommon.util.CmsException
import com.mbd.cmscommon.util.FileReadErrors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.awt.ComposeWindow
import com.mbd.cmscommon.controller.studentsForTab
import com.mbd.cmscommon.domain.model.DailyAttendanceMark
import com.mbd.cmscommon.domain.model.Session
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/** Month-by-month attendance register for one session/course, reachable from [MarkAttendanceScreen]. */
@Composable
fun AttendanceHistoryScreen(
    sessionId: String,
    courseCode: String,
    initialMonth: YearMonth?,
    /** The class's shift; null shows the whole session. */
    shift: Session?,
    sessionRepository: AcademicSessionRepository,
    attendanceRepository: SessionAttendanceRepository,
    editRequestRepository: AttendanceEditRequestRepository,
    departmentRepository: DepartmentRepository,
    curriculumRepository: CurriculumRepository,
    timetableRepository: SessionTimetableRepository,
    window: ComposeWindow,
    onOpenStudent: (studentSessionId: String, rollNumber: String, month: YearMonth) -> Unit,
) {
    // [sessionId] is the primary session, or a comma-joined list of every session of a merged class.
    val sessionIds = remember(sessionId) { sessionId.split(',').filter { it.isNotBlank() } }
    val primaryId = sessionIds.first()
    val roster by remember(sessionId, shift) {
        kotlinx.coroutines.flow.combine(sessionIds.map { sid -> sessionRepository.observeStudents(sid).map { studentsForTab(it, shift) } }) { it.toList().flatten() }
    }.collectAsState(initial = emptyList())
    var month by remember { mutableStateOf(initialMonth ?: YearMonth.now()) }
    var pendingCells by remember { mutableStateOf<Set<Pair<String, LocalDate>>>(emptySet()) }
    var requestState by remember { mutableStateOf<Outcome<Unit>?>(null) }
    val scope = rememberCoroutineScope()
    var marks by remember { mutableStateOf<Map<String, Map<LocalDate, DailyAttendanceMark>>>(emptyMap()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val session by sessionRepository.observeSession(primaryId).collectAsState(initial = null)
    val monthLabel = remember(month) { "${month.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)} ${month.year}" }


    LaunchedEffect(sessionId, courseCode, month, shift) {
        loading = true
        error = null
        try {
            val from = month.atDay(1)
            val to = month.atEndOfMonth()
            val loaded = mutableMapOf<String, Map<LocalDate, DailyAttendanceMark>>()
            val pending = mutableSetOf<Pair<String, LocalDate>>()
            for (sid in sessionIds) {
                val classRolls = studentsForTab(sessionRepository.observeStudents(sid).first(), shift).map { it.rollNumber }.toSet()
                attendanceRepository.marksBetween(sid, courseCode, from, to)
                    .filter { shift == null || it.rollNumber in classRolls }
                    .groupBy { it.rollNumber }
                    .forEach { (roll, ms) -> loaded[com.mbd.cmscommon.domain.model.SessionStudent.buildId(sid, roll)] = ms.associateBy { it.date } }
                runCatching { editRequestRepository.getPendingFor(sid, courseCode, from, to) }
                    .getOrElse {
                        error = it.userMessageLogged("AttendanceHistoryScreen.loadPending", "Couldn't load the pending attendance edit requests for $courseCode.")
                        emptyList()
                    }
                    .forEach { pending += com.mbd.cmscommon.domain.model.SessionStudent.buildId(sid, it.rollNumber) to it.date }
            }
            marks = loaded
            pendingCells = pending
        } catch (t: Throwable) {
            error = t.userMessageLogged("AttendanceHistoryScreen.load", "Couldn't load the $courseCode attendance history for ${month.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${month.year}.")
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
        onOpenStudent = { student -> onOpenStudent(student.sessionId, student.rollNumber, month) },
        onSubmitEditRequest = { student, date, current, status, late, reason ->
            val roll = student.rollNumber
            if (requestState !is Outcome.Loading) {
                requestState = Outcome.Loading
                scope.launch {
                    requestState = try {
                        val semester = sessionRepository.observeSession(student.sessionId).first()?.currentSemester
                            ?: throw CmsException.NotFound("This session could not be found. Refresh and try again.")
                        editRequestRepository.submitRequest(
                            sessionId = student.sessionId,
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
                        pendingCells = pendingCells + (student.id to date)
                        Outcome.Success(Unit)
                    } catch (t: Throwable) {
                        Outcome.Error(t.userMessageLogged("AttendanceHistoryScreen.submitEditRequest", "Couldn't send the attendance edit request for $roll on $date."), t)
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
                val context = resolveRegisterContext(session, courseCode, departmentRepository, curriculumRepository, timetableRepository, shift)
                DocumentExporter.export(window, attendanceRegisterExport(courseCode, session, month, roster, marks, context, shift), format)
              } catch (t: Throwable) {
                error = FileReadErrors.describeWrite(t, format.label)
              }
            }
        },
        errorMessage = error,
        onClearError = { error = null },
    )
}
