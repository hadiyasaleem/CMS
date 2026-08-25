package com.mbd.cmsdesktop.ui.teacher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.controller.ExamPaperSubmissionController
import com.mbd.cmscommon.controller.ExamsHubController
import com.mbd.cmscommon.controller.InsightsController
import com.mbd.cmscommon.controller.LinkRequestsController
import com.mbd.cmscommon.controller.MarkAttendanceController
import com.mbd.cmscommon.controller.MarksEntryController
import com.mbd.cmscommon.controller.NotificationPublisherKind
import com.mbd.cmscommon.controller.SemesterResultsController
import com.mbd.cmscommon.domain.model.NotificationTargetRole
import com.mbd.cmscommon.domain.model.UserRole
import com.mbd.cmscommon.domain.model.teacherHomeSnapshot
import com.mbd.cmscommon.domain.model.teacherMenuSnapshot
import com.mbd.cmscommon.ui.components.ExamPaperSubmissionWorkspace
import com.mbd.cmscommon.ui.components.ExamsDestination
import com.mbd.cmscommon.ui.components.ExamsHubWorkspace
import com.mbd.cmscommon.ui.components.InsightsViewer
import com.mbd.cmscommon.ui.components.InsightsWorkspace
import com.mbd.cmscommon.ui.components.LinkRequestReviewWorkspace
import com.mbd.cmscommon.ui.components.MarkAttendanceWorkspace
import com.mbd.cmscommon.ui.components.MarksEntryWorkspace
import com.mbd.cmscommon.ui.components.NotificationControllerWorkspace
import com.mbd.cmscommon.ui.components.SemesterResultsWorkspace
import com.mbd.cmscommon.ui.components.TeacherHomeDestination
import com.mbd.cmscommon.ui.components.TeacherHomeWorkspace
import com.mbd.cmscommon.ui.components.TeacherMenuWorkspace
import com.mbd.cmscommon.ui.components.TeacherProfileWorkspace
import com.mbd.cmscommon.ui.components.TeacherScheduleWorkspace
import com.mbd.cmscommon.ui.components.TeacherStudentRosterWorkspace
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmsdesktop.di.DesktopAppComponent
import java.awt.Window
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import com.mbd.cmscommon.util.userMessage
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private enum class TeacherTab(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Filled.Home),
    Attendance("Attend", Icons.Filled.FactCheck),
    ExamsHub("Exams", Icons.Filled.MenuBook),
    Schedule("Schedule", Icons.Filled.CalendarMonth),
    MenuHub("Menu", Icons.Filled.Menu),
}

private sealed interface TeacherLeaf {
    data object Marks : TeacherLeaf
    data object SemesterResults : TeacherLeaf
    data object ExamPaper : TeacherLeaf
    data object Notifications : TeacherLeaf
    data object LinkRequests : TeacherLeaf
    data object MyStudents : TeacherLeaf
    data object Insights : TeacherLeaf
    data object Profile : TeacherLeaf
    data class AttendanceHistory(val sessionId: String, val courseCode: String) : TeacherLeaf
}

/**
 * Top-level shell for the teacher desktop app: a [NavigationRail] over the 5 mobile-parity tabs
 * (see `TeacherDestination.bottomNavItems` in mobile-teacher) + the directly-reachable leaves.
 * Events/Datesheets/Documents (shared calendar/document/datesheet workspaces used from the
 * MenuHub/ExamsHub on mobile) are not yet wired here — those workspaces need a resolved viewer
 * role combined from several flows the same way mobile's EventsViewModel/DocumentsViewModel/
 * DatesheetsViewModel do it; left as a follow-up so this pass could cover the 5 tabs + their
 * direct children first. Attendance history CSV/PDF export is Android-only (uses
 * `android.graphics.pdf.PdfDocument` / `Context`) and is stubbed as a no-op here.
 */
@Composable
fun TeacherNavHost(role: UserRole.Teacher, component: DesktopAppComponent, window: Window, onSignOut: () -> Unit) {
    val scope = rememberCoroutineScope()
    var tab by remember { mutableStateOf(TeacherTab.Home) }
    var leaf by remember { mutableStateOf<TeacherLeaf?>(null) }
    val teacherId = component.sessionManager().accountKey.orEmpty()

    Row(Modifier.fillMaxSize()) {
        NavigationRail(containerColor = CmsTheme.colors.ink, contentColor = CmsTheme.colors.onInk) {
            Spacer(Modifier.height(16.dp))
            TeacherTab.entries.forEach { t ->
                NavigationRailItem(
                    selected = leaf == null && tab == t,
                    onClick = { tab = t; leaf = null },
                    icon = { Icon(t.icon, contentDescription = t.label) },
                    label = { Text(t.label) },
                )
            }
        }
        Box(Modifier.weight(1f).fillMaxHeight().padding(24.dp)) {
            val currentLeaf = leaf
            if (currentLeaf != null) {
                Column(Modifier.fillMaxSize()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { leaf = null }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Text(leafTitle(currentLeaf), style = MaterialTheme.typography.titleLarge)
                    }
                    Spacer(Modifier.height(16.dp))
                    when (currentLeaf) {
                        TeacherLeaf.Marks -> {
                            val controller = remember(component) {
                                MarksEntryController(
                                    component.sessionMarksRepository(),
                                    component.academicSessionRepository(),
                                    component.markEditRequestRepository(),
                                    teacherId,
                                    scope,
                                )
                            }
                            val assignments by component.teacherAssignmentsProvider().observeMyAssignments()
                                .collectAsState(initial = emptyList())
                            val selected by controller.selected.collectAsState()
                            val examType by controller.examType.collectAsState()
                            val roster by controller.roster.collectAsState()
                            val scores by controller.displayScores.collectAsState()
                            val lockedRolls by controller.lockedRolls.collectAsState()
                            val pendingByRoll by controller.pendingByRoll.collectAsState()
                            val absentRolls by controller.absentRolls.collectAsState()
                            val savedAbsentRolls by controller.savedAbsentRolls.collectAsState()
                            val saveState by controller.saveState.collectAsState()
                            val requestState by controller.requestState.collectAsState()
                            MarksEntryWorkspace(
                                assignments = assignments,
                                selected = selected,
                                examType = examType,
                                roster = roster,
                                scores = scores,
                                lockedRolls = lockedRolls,
                                pendingByRoll = pendingByRoll,
                                absentRolls = absentRolls,
                                savedAbsentRolls = savedAbsentRolls,
                                saveOutcome = saveState ?: com.mbd.cmscommon.util.Outcome.Success(Unit),
                                requestOutcome = requestState ?: com.mbd.cmscommon.util.Outcome.Success(Unit),
                                onSelect = controller::select,
                                onExamType = controller::selectExamType,
                                onScore = controller::setScore,
                                onToggleAbsent = controller::toggleAbsent,
                                onSave = controller::save,
                                onClearRequestState = controller::clearRequestState,
                                onRequestEdit = controller::requestMarkEdit,
                            )
                        }
                        TeacherLeaf.SemesterResults -> {
                            val controller = remember(component) {
                                SemesterResultsController(
                                    component.sessionMarksRepository(),
                                    component.academicSessionRepository(),
                                    component.curriculumRepository(),
                                    component.teacherAssignmentsProvider().observeMyAssignments(),
                                    scope,
                                )
                            }
                            val sessions by controller.sessions.collectAsState()
                            val sessionId by controller.sessionId.collectAsState()
                            val semester by controller.semester.collectAsState()
                            val roster by controller.roster.collectAsState()
                            val results by controller.results.collectAsState()
                            val subjects by controller.subjects.collectAsState()
                            val saveState by controller.saveState.collectAsState()
                            val loadState by controller.loadState.collectAsState()
                            SemesterResultsWorkspace(
                                sessions = sessions,
                                sessionId = sessionId,
                                semester = semester,
                                roster = roster,
                                results = results,
                                subjects = subjects,
                                saveOutcome = saveState ?: com.mbd.cmscommon.util.Outcome.Success(Unit),
                                loadOutcome = loadState ?: com.mbd.cmscommon.util.Outcome.Success(Unit),
                                onSelectSession = controller::selectSession,
                                onSemester = controller::setSemester,
                                onRetry = controller::refresh,
                                onClearSave = controller::clearSave,
                                onRecord = controller::record,
                            )
                        }
                        TeacherLeaf.ExamPaper -> {
                            val controller = remember(component) {
                                ExamPaperSubmissionController(component.examPaperSubmissionRepository(), teacherId, scope)
                            }
                            val assignments by component.teacherAssignmentsProvider().observeMyAssignments()
                                .collectAsState(initial = emptyList())
                            val selected by controller.selected.collectAsState()
                            val examType by controller.examType.collectAsState()
                            val submissions by controller.submissions.collectAsState()
                            val uploadState by controller.uploadState.collectAsState()
                            ExamPaperSubmissionWorkspace(
                                assignments = assignments,
                                selected = selected,
                                examType = examType,
                                submissions = submissions,
                                outcome = uploadState ?: com.mbd.cmscommon.util.Outcome.Success(Unit),
                                onSelect = controller::select,
                                onExamType = controller::selectExamType,
                                onChooseFile = {
                                    val dialog = java.awt.FileDialog(window as? java.awt.Frame, "Choose exam paper (PDF)", java.awt.FileDialog.LOAD)
                                    dialog.file = "*.pdf"
                                    dialog.isVisible = true
                                    val dir: String? = dialog.directory
                                    val name: String? = dialog.file
                                    if (dir != null && name != null) {
                                        val bytes = java.io.File(dir, name).readBytes()
                                        scope.launch { controller.upload(bytes, name) }
                                    }
                                },
                                onOpen = { submission ->
                                    controller.downloadAndOpen(submission, java.io.File(System.getProperty("java.io.tmpdir"))) { file ->
                                        runCatching { java.awt.Desktop.getDesktop().open(file) }
                                    }
                                },
                                onDelete = controller::deleteSubmission,
                            )
                        }
                        TeacherLeaf.Notifications -> {
                            val controller = remember(component) {
                                com.mbd.cmscommon.controller.NotificationsController(
                                    repository = component.notificationRepository(),
                                    viewerRole = NotificationTargetRole.TEACHER,
                                    accountKey = teacherId,
                                    sessionRepository = component.academicSessionRepository(),
                                    departmentRepository = component.departmentRepository(),
                                    publisherKind = NotificationPublisherKind.TEACHER,
                                    scope = scope,
                                )
                            }
                            NotificationControllerWorkspace(controller = controller)
                        }
                        TeacherLeaf.LinkRequests -> {
                            val controller = remember(component) {
                                LinkRequestsController(
                                    repository = component.studentLinkRequestRepository(),
                                    sessionRepository = component.academicSessionRepository(),
                                    departmentRepository = component.departmentRepository(),
                                    reviewerId = teacherId,
                                    scope = scope,
                                )
                            }
                            val requests by controller.requests.collectAsState()
                            val sessions by controller.sessions.collectAsState()
                            val departments by controller.departments.collectAsState()
                            val verifications by controller.verifications.collectAsState()
                            val access by controller.access.collectAsState()
                            val loading by controller.loading.collectAsState()
                            val busyRequestId by controller.busyRequestId.collectAsState()
                            val rowErrors by controller.rowErrors.collectAsState()
                            val notice by controller.notice.collectAsState()
                            val error by controller.error.collectAsState()
                            LinkRequestReviewWorkspace(
                                requests = requests,
                                sessions = sessions,
                                departments = departments,
                                verifications = verifications,
                                access = access,
                                loading = loading,
                                busyRequestId = busyRequestId,
                                rowErrors = rowErrors,
                                notice = notice,
                                errorMessage = error,
                                onRefresh = controller::refresh,
                                onApprove = controller::approve,
                                onReject = controller::reject,
                                onConsumeNotice = controller::consumeNotice,
                                onClearError = controller::clearError,
                            )
                        }
                        TeacherLeaf.MyStudents -> {
                            val assignments by component.teacherAssignmentsProvider().observeMyAssignments()
                                .collectAsState(initial = emptyList())
                            var selected by remember { mutableStateOf<com.mbd.cmscommon.teacher.ResolvedAssignment?>(null) }
                            val students by remember(component, selected) {
                                val assignment = selected
                                val flow: kotlinx.coroutines.flow.Flow<List<com.mbd.cmscommon.domain.model.SessionStudent>> =
                                    if (assignment == null) {
                                        flowOf(emptyList())
                                    } else {
                                        component.academicSessionRepository().observeStudents(assignment.sessionId)
                                    }
                                flow
                            }.collectAsState(initial = emptyList())
                            val tallies by remember(component, selected) {
                                val assignment = selected
                                val flow: kotlinx.coroutines.flow.Flow<Map<String, com.mbd.cmscommon.domain.model.AttendanceTally>> =
                                    if (assignment == null) {
                                        flowOf(emptyMap())
                                    } else {
                                        component.sessionAttendanceRepository().observeTallies(assignment.sessionId, assignment.courseCode)
                                            .map { list -> list.associateBy { it.rollNumber } }
                                    }
                                flow
                            }.collectAsState(initial = emptyMap())
                            TeacherStudentRosterWorkspace(
                                assignments = assignments,
                                selected = selected,
                                students = students,
                                tallies = tallies,
                                onSelectAssignment = { selected = it },
                            )
                        }
                        TeacherLeaf.Insights -> {
                            val controller = remember(component) { InsightsController(component.insightsRepository(), scope) }
                            val overviews by controller.overviews.collectAsState()
                            val atRisk by controller.atRisk.collectAsState()
                            val examStats by controller.examStats.collectAsState()
                            val sessions by component.academicSessionRepository().observeAllSessions().collectAsState(initial = emptyList())
                            val departments by component.departmentRepository().observeActiveDepartments().collectAsState(initial = emptyList())
                            val assignments by component.teacherAssignmentsProvider().observeMyAssignments()
                                .collectAsState(initial = emptyList())
                            val loading by controller.refreshing.collectAsState()
                            val error by controller.error.collectAsState()
                            InsightsWorkspace(
                                overviews = overviews.orEmpty(),
                                atRisk = atRisk.orEmpty(),
                                examStats = examStats.orEmpty(),
                                sessions = sessions,
                                departments = departments,
                                viewer = InsightsViewer.TEACHER,
                                assignments = assignments,
                                loading = loading,
                                errorMessage = error,
                                onRetry = controller::refresh,
                            )
                        }
                        TeacherLeaf.Profile -> {
                            val profileFlow: kotlinx.coroutines.flow.Flow<com.mbd.cmscommon.domain.model.Teacher?> =
                                component.teacherRepository().observeTeacher(teacherId)
                            val profile by profileFlow.collectAsState(initial = null)
                            val assignments by component.teacherAssignmentsProvider().observeMyAssignments()
                                .collectAsState(initial = emptyList())
                            var departmentName by remember { mutableStateOf<String?>(null) }
                            var actionMessage by remember { mutableStateOf<String?>(null) }
                            var errorMessage by remember { mutableStateOf<String?>(null) }
                            androidx.compose.runtime.LaunchedEffect(profile?.deptId) {
                                val deptId = profile?.deptId
                                departmentName = if (deptId.isNullOrBlank()) null else {
                                    runCatching { component.departmentRepository().getDepartment(deptId) }.getOrNull()?.name
                                }
                            }
                            TeacherProfileWorkspace(
                                profile = profile,
                                accountKey = teacherId,
                                departmentName = departmentName,
                                assignments = assignments,
                                loading = profile == null,
                                errorMessage = errorMessage,
                                actionMessage = actionMessage,
                                onResetPassword = {
                                    scope.launch {
                                        runCatching { component.sessionManager().sendPasswordReset(teacherId) }
                                            .onSuccess { actionMessage = "Password reset email sent." }
                                            .onFailure { errorMessage = it.message }
                                    }
                                },
                                onSignOut = onSignOut,
                            )
                        }
                        is TeacherLeaf.AttendanceHistory -> {
                            TeacherAttendanceHistoryContent(
                                sessionId = currentLeaf.sessionId,
                                courseCode = currentLeaf.courseCode,
                                component = component,
                                scope = scope,
                            )
                        }
                    }
                }
            } else {
                when (tab) {
                    TeacherTab.Home -> {
                        val snapshot by remember(component) {
                            combine(
                                component.sessionTimetableRepository().observeMyPeriods(teacherId),
                                component.teacherAssignmentsProvider().observeMyAssignments(),
                            ) { periods, assignments ->
                                teacherHomeSnapshot(teacherId, periods, assignments, LocalDate.now(), LocalTime.now())
                            }.stateIn(
                                scope,
                                SharingStarted.WhileSubscribed(5_000),
                                teacherHomeSnapshot(teacherId, emptyList(), emptyList(), LocalDate.now(), LocalTime.now()),
                            )
                        }.collectAsState()
                        TeacherHomeWorkspace(
                            heroPainter = painterResource("splash_postgraduate_block.jpg"),
                            snapshot = snapshot,
                            onOpen = { destination ->
                                when (destination) {
                                    TeacherHomeDestination.ATTENDANCE -> tab = TeacherTab.Attendance
                                    TeacherHomeDestination.MARKS -> leaf = TeacherLeaf.Marks
                                    TeacherHomeDestination.EXAM_PAPER -> leaf = TeacherLeaf.ExamPaper
                                    TeacherHomeDestination.STUDENTS -> leaf = TeacherLeaf.MyStudents
                                    TeacherHomeDestination.SCHEDULE -> tab = TeacherTab.Schedule
                                    TeacherHomeDestination.NOTIFICATIONS -> leaf = TeacherLeaf.Notifications
                                }
                            },
                        )
                    }
                    TeacherTab.Attendance -> {
                        val controller = remember(component) {
                            MarkAttendanceController(
                                component.sessionAttendanceRepository(),
                                component.academicSessionRepository(),
                                component.notificationRepository(),
                                teacherId,
                                scope,
                            )
                        }
                        val assignments by component.teacherAssignmentsProvider().observeMyAssignments()
                            .collectAsState(initial = emptyList())
                        val selected by controller.selected.collectAsState()
                        val roster by controller.roster.collectAsState()
                        val termPercents by controller.termPercents.collectAsState()
                        val statuses by controller.statuses.collectAsState()
                        val lateRolls by controller.late.collectAsState()
                        val remarks by controller.remarks.collectAsState()
                        val alreadyMarked by controller.alreadyMarked.collectAsState()
                        val allMarked by controller.allMarked.collectAsState()
                        val lectureTopic by controller.lectureTopic.collectAsState()
                        val submitState by controller.submitState.collectAsState()
                        MarkAttendanceWorkspace(
                            heroPainter = painterResource("splash_postgraduate_block.jpg"),
                            assignments = assignments,
                            selected = selected,
                            roster = roster,
                            termPercents = termPercents,
                            statuses = statuses,
                            lateRolls = lateRolls,
                            remarks = remarks,
                            alreadyMarked = alreadyMarked,
                            allMarked = allMarked,
                            lectureTopic = lectureTopic,
                            outcome = submitState ?: com.mbd.cmscommon.util.Outcome.Success(Unit),
                            onSelect = controller::select,
                            onStatus = controller::setStatus,
                            onToggleLate = controller::toggleLate,
                            onRemark = controller::setRemark,
                            onLectureTopic = controller::setLectureTopic,
                            onHistory = { sessionId, courseCode -> leaf = TeacherLeaf.AttendanceHistory(sessionId, courseCode) },
                            onSubmit = controller::submit,
                        )
                    }
                    TeacherTab.ExamsHub -> {
                        val controller = remember(component) {
                            ExamsHubController(
                                teacherId,
                                component.teacherAssignmentsProvider(),
                                component.examPaperSubmissionRepository(),
                                component.datesheetRepository(),
                                scope,
                            )
                        }
                        val snapshot by controller.snapshot.collectAsState()
                        val loading by controller.loading.collectAsState()
                        val loadError by controller.loadError.collectAsState()
                        ExamsHubWorkspace(
                            heroPainter = painterResource("splash_postgraduate_block.jpg"),
                            snapshot = snapshot,
                            loading = loading,
                            errorMessage = loadError,
                            onRetry = controller::refresh,
                            onOpen = { destination ->
                                when (destination) {
                                    ExamsDestination.MARKS -> leaf = TeacherLeaf.Marks
                                    ExamsDestination.EXAM_PAPER -> leaf = TeacherLeaf.ExamPaper
                                    ExamsDestination.RESULTS -> leaf = TeacherLeaf.SemesterResults
                                    ExamsDestination.DATESHEETS -> {}
                                }
                            },
                        )
                    }
                    TeacherTab.Schedule -> {
                        val periods by component.sessionTimetableRepository().observeMyPeriods(teacherId)
                            .collectAsState(initial = emptyList())
                        val sessions by component.academicSessionRepository().observeAllSessions()
                            .collectAsState(initial = emptyList())
                        var selectedDay by remember { mutableStateOf(DayOfWeek.MONDAY) }
                        var outcome by remember {
                            mutableStateOf<com.mbd.cmscommon.util.Outcome<Unit>>(com.mbd.cmscommon.util.Outcome.Success(Unit))
                        }
                        TeacherScheduleWorkspace(
                            heroPainter = painterResource("splash_postgraduate_block.jpg"),
                            periods = periods,
                            sessions = sessions,
                            selectedDay = selectedDay,
                            outcome = outcome,
                            onSelectDay = { selectedDay = it },
                            onRefresh = {
                                scope.launch {
                                    outcome = com.mbd.cmscommon.util.Outcome.Loading
                                    outcome = try {
                                        periods.map { it.sessionId }.distinct().forEach { sessionId ->
                                            runCatching { component.sessionTimetableRepository().syncSession(sessionId) }
                                        }
                                        com.mbd.cmscommon.util.Outcome.Success(Unit)
                                    } catch (t: Throwable) {
                                        com.mbd.cmscommon.util.Outcome.Error(
                                            t.userMessage("Refresh failed. Please try again."),
                                            t,
                                        )
                                    }
                                }
                            },
                        )
                    }
                    TeacherTab.MenuHub -> {
                        val snapshot by remember(component) {
                            combine(
                                component.teacherRepository().observeTeacher(teacherId),
                                component.teacherAssignmentsProvider().observeMyAssignments(),
                                component.notificationRepository().observeUnreadCount(NotificationTargetRole.TEACHER),
                                component.studentLinkRequestRepository().observePendingRequests(),
                            ) { profile, assignments, unread, pending ->
                                teacherMenuSnapshot(profile, assignments, unread, pending.size)
                            }.stateIn(
                                scope,
                                SharingStarted.WhileSubscribed(5_000),
                                teacherMenuSnapshot(null, emptyList(), 0, 0),
                            )
                        }.collectAsState()
                        TeacherMenuWorkspace(
                            heroPainter = painterResource("splash_postgraduate_block.jpg"),
                            snapshot = snapshot,
                            onOpenMyStudents = { leaf = TeacherLeaf.MyStudents },
                            onOpenCalendar = {},
                            onOpenDocuments = {},
                            onOpenInsights = { leaf = TeacherLeaf.Insights },
                            onOpenLinkRequests = { leaf = TeacherLeaf.LinkRequests },
                            onOpenNotifications = { leaf = TeacherLeaf.Notifications },
                            onOpenProfile = { leaf = TeacherLeaf.Profile },
                            onSignOut = onSignOut,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Manual replica of mobile's `AttendanceHistoryViewModel`: loads a month of daily marks for
 * [sessionId]/[courseCode] and lets the user page between months. CSV/PDF export
 * (`AttendanceExporter` on mobile) relies on `android.graphics.pdf.PdfDocument` and Android
 * `Context`, so it isn't ported here — both export actions are no-ops on desktop for now.
 */
@Composable
private fun TeacherAttendanceHistoryContent(
    sessionId: String,
    courseCode: String,
    component: DesktopAppComponent,
    scope: kotlinx.coroutines.CoroutineScope,
) {
    var month by remember(sessionId, courseCode) { mutableStateOf(LocalDate.now().withDayOfMonth(1)) }
    var loading by remember { mutableStateOf(true) }
    var marks by remember(sessionId, courseCode) {
        mutableStateOf<Map<String, Map<LocalDate, com.mbd.cmscommon.domain.model.DailyAttendanceMark>>>(emptyMap())
    }
    val roster by component.academicSessionRepository().observeStudents(sessionId).collectAsState(initial = emptyList())
    val monthLabel = remember(month) {
        month.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy", java.util.Locale.ENGLISH))
    }

    androidx.compose.runtime.LaunchedEffect(sessionId, courseCode, month) {
        loading = true
        try {
            val from = month
            val to = from.withDayOfMonth(from.lengthOfMonth())
            val dailyMarks = component.sessionAttendanceRepository().marksBetween(sessionId, courseCode, from, to)
            marks = dailyMarks.groupBy { it.rollNumber }.mapValues { (_, ms) -> ms.associateBy { it.date } }
        } finally {
            loading = false
        }
    }

    com.mbd.cmscommon.ui.components.AttendanceHistoryWorkspace(
        courseCode = courseCode,
        monthLabel = monthLabel,
        loading = loading,
        roster = roster,
        marks = marks,
        onPreviousMonth = { month = month.minusMonths(1) },
        onNextMonth = { month = month.plusMonths(1) },
        onExportCsv = {},
        onExportPdf = {},
    )
}

private fun leafTitle(leaf: TeacherLeaf): String = when (leaf) {
    TeacherLeaf.Marks -> "Marks entry"
    TeacherLeaf.SemesterResults -> "Semester results"
    TeacherLeaf.ExamPaper -> "Submit exam paper"
    TeacherLeaf.Notifications -> "Notifications"
    TeacherLeaf.LinkRequests -> "Link requests"
    TeacherLeaf.MyStudents -> "My students"
    TeacherLeaf.Insights -> "Insights"
    TeacherLeaf.Profile -> "Profile"
    is TeacherLeaf.AttendanceHistory -> "Attendance history — ${leaf.courseCode}"
}
