package com.mbd.cmsdesktop.ui.student

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Grading
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
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
import com.mbd.cmscommon.controller.NotificationPublisherKind
import com.mbd.cmscommon.controller.NotificationsController
import com.mbd.cmscommon.controller.StudentAttendanceController
import com.mbd.cmscommon.controller.StudentExamsHubController
import com.mbd.cmscommon.controller.StudentFeeChallanController
import com.mbd.cmscommon.controller.StudentHomeController
import com.mbd.cmscommon.controller.StudentMarksController
import com.mbd.cmscommon.controller.StudentMoreController
import com.mbd.cmscommon.controller.StudentProfileController
import com.mbd.cmscommon.controller.StudentResultsController
import com.mbd.cmscommon.controller.StudentTimetableController
import com.mbd.cmscommon.controller.studentAttendanceSnapshot
import com.mbd.cmscommon.controller.studentMarksSnapshot
import com.mbd.cmscommon.domain.model.CalendarViewerContext
import com.mbd.cmscommon.domain.model.CalendarViewerRole
import com.mbd.cmscommon.domain.model.DocumentViewerContext
import com.mbd.cmscommon.domain.model.DocumentViewerRole
import com.mbd.cmscommon.domain.model.NotificationTargetRole
import com.mbd.cmscommon.domain.model.UserRole
import com.mbd.cmscommon.domain.model.studentFeeSnapshot
import com.mbd.cmscommon.domain.model.studentHomeSnapshot
import com.mbd.cmscommon.domain.model.studentResultsSnapshot
import com.mbd.cmscommon.domain.model.studentTimetableSnapshot
import com.mbd.cmscommon.ui.components.CalendarWorkspace
import com.mbd.cmscommon.ui.components.DocumentWorkspace
import com.mbd.cmscommon.ui.components.NotificationControllerWorkspace
import com.mbd.cmscommon.ui.components.StudentExamsDestination
import com.mbd.cmscommon.ui.components.StudentExamsHubWorkspace
import com.mbd.cmscommon.ui.components.StudentFeeWorkspace
import com.mbd.cmscommon.ui.components.StudentHomeDestination
import com.mbd.cmscommon.ui.components.StudentHomeWorkspace
import com.mbd.cmscommon.ui.components.StudentAttendanceWorkspace
import com.mbd.cmscommon.ui.components.StudentMarksWorkspace
import com.mbd.cmscommon.ui.components.StudentMoreDestination
import com.mbd.cmscommon.ui.components.StudentMoreWorkspace
import com.mbd.cmscommon.ui.components.StudentOwnProfileWorkspace
import com.mbd.cmscommon.ui.components.StudentResultsWorkspace
import com.mbd.cmscommon.ui.components.StudentTimetableWorkspace
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmsdesktop.di.DesktopAppComponent
import java.awt.Window
import java.time.LocalDate
import kotlinx.coroutines.launch

private enum class StudentTab(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Filled.Home),
    Attendance("Attendance", Icons.Filled.FactCheck),
    ExamsHub("Exams", Icons.Filled.Grading),
    Timetable("Timetable", Icons.Filled.CalendarMonth),
    More("More", Icons.Filled.Menu),
}

private sealed interface StudentLeaf {
    data object Marks : StudentLeaf
    data object Results : StudentLeaf
    data object Events : StudentLeaf
    data object Datesheets : StudentLeaf
    data object Documents : StudentLeaf
    data object Fees : StudentLeaf
    data object Notifications : StudentLeaf
    data object Profile : StudentLeaf
}

/**
 * Top-level shell for the student desktop app: a [NavigationRail] over the 5 mobile-parity tabs
 * (see `StudentDestination.bottomNavItems` in mobile-student) + the directly-reachable leaves
 * (Marks, Results, Events, Datesheets, Documents, Fees, Notifications, Profile). Mirrors
 * `StudentNavHost` and each feature package in mobile-student, with `CurrentStudentProvider`
 * (mobile-only, Hilt-scoped) replaced by [StudentContextHolder] built directly from
 * `role.studentId`. The mobile-only account-link-request flow is intentionally not present here -
 * see [UnlinkedStudentScreen].
 *
 * The Events/Datesheets/Documents leaves reuse the shared (non-role-specific) `CalendarWorkspace`
 * / `DatesheetWorkspace` / `DocumentWorkspace` components in read-only mode (no create/manage
 * affordances for students, matching mobile's `EventsScreen`/`DatesheetsScreen`/`DocumentsScreen`).
 */
@Composable
fun StudentNavHost(role: UserRole.LinkedStudent, component: DesktopAppComponent, window: Window, onSignOut: () -> Unit) {
    val scope = rememberCoroutineScope()
    var tab by remember { mutableStateOf(StudentTab.Home) }
    var leaf by remember { mutableStateOf<StudentLeaf?>(null) }
    val accountKey = component.sessionManager().accountKey.orEmpty()

    val contextHolder = remember(component, role.studentId) {
        StudentContextHolder(component.academicSessionRepository(), role.studentId, scope)
    }
    val sessionId = contextHolder.sessionId
    val deptId = contextHolder.deptId
    val rollNumber = contextHolder.rollNumber
    val studentContext by contextHolder.context.collectAsState()

    Row(Modifier.fillMaxSize()) {
        NavigationRail(containerColor = CmsTheme.colors.ink, contentColor = CmsTheme.colors.onInk) {
            Spacer(Modifier.height(16.dp))
            StudentTab.entries.forEach { t ->
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
                        StudentLeaf.Marks -> {
                            val controller = remember(component, sessionId, rollNumber) {
                                StudentMarksController(sessionId, rollNumber, component.sessionMarksRepository(), component.curriculumRepository(), scope)
                            }
                            val rows by controller.rows.collectAsState()
                            val refreshing by controller.refreshing.collectAsState()
                            StudentMarksWorkspace(
                                snapshot = studentMarksSnapshot(rows),
                                loading = refreshing && rows.isEmpty(),
                                errorMessage = null,
                                onRetry = controller::refresh,
                            )
                        }
                        StudentLeaf.Results -> {
                            val controller = remember(component, sessionId, rollNumber) {
                                StudentResultsController(sessionId, rollNumber, component.sessionMarksRepository(), scope)
                            }
                            val results by controller.results.collectAsState()
                            val loading by controller.loading.collectAsState()
                            StudentResultsWorkspace(
                                snapshot = if (loading && results.isEmpty()) null else studentResultsSnapshot(results),
                                loading = loading,
                                errorMessage = null,
                                onRetry = controller::refresh,
                            )
                        }
                        StudentLeaf.Events -> {
                            val controller = remember(component) {
                                com.mbd.cmscommon.controller.CalendarController(component.calendarRepository(), accountKey, scope)
                            }
                            val events by controller.events.collectAsState()
                            val loading by controller.loading.collectAsState()
                            val busy by controller.busy.collectAsState()
                            val actionMessage by controller.actionMessage.collectAsState()
                            CalendarWorkspace(
                                events = events.orEmpty(),
                                viewer = CalendarViewerContext(CalendarViewerRole.STUDENT, deptId, setOf(sessionId)),
                                departments = emptyList(),
                                sessions = emptyList(),
                                canEdit = false,
                                loading = loading,
                                busy = busy,
                                errorMessage = null,
                                actionMessage = actionMessage,
                                onRetry = controller::refresh,
                                onCreate = {},
                                onDelete = {},
                            )
                        }
                        StudentLeaf.Datesheets -> {
                            val controller = remember(component, accountKey) {
                                com.mbd.cmscommon.controller.DatesheetsController(component.datesheetRepository(), accountKey, scope)
                            }
                            val sheets by controller.sheets.collectAsState()
                            val slots by controller.slots.collectAsState()
                            val loadingSlots by controller.loadingSlots.collectAsState()
                            val refreshing by controller.refreshing.collectAsState()
                            val busy by controller.busy.collectAsState()
                            val actionMessage by controller.actionMessage.collectAsState()
                            com.mbd.cmscommon.ui.components.DatesheetWorkspace(
                                datesheets = sheets.orEmpty().filter { it.published && (it.sessionId == null || it.sessionId == sessionId) },
                                slots = slots,
                                loadingSlots = loadingSlots,
                                sessions = emptyList(),
                                subjectsBySession = emptyMap(),
                                invigilators = emptyList(),
                                viewer = com.mbd.cmscommon.domain.model.DatesheetViewerContext(
                                    role = com.mbd.cmscommon.domain.model.DatesheetViewerRole.STUDENT,
                                    sessionId = sessionId,
                                    canManage = false,
                                ),
                                loading = refreshing,
                                busy = busy,
                                errorMessage = null,
                                actionMessage = actionMessage,
                                onRetry = controller::refresh,
                                onLoadSlots = { id -> controller.loadSlots(id) },
                                onLoadSubjects = {},
                                onCreate = {},
                                onUpdate = { _, _ -> },
                                onSetPublished = { _, _ -> },
                                onDelete = {},
                                onAddSlot = {},
                                onUpdateSlot = {},
                                onDeleteSlot = { _, _ -> },
                            )
                        }
                        StudentLeaf.Documents -> {
                            val controller = remember(component) {
                                com.mbd.cmscommon.controller.DocumentsController(component.documentRepository(), scope)
                            }
                            val docs by controller.docs.collectAsState()
                            val loading by controller.loading.collectAsState()
                            val busy by controller.busy.collectAsState()
                            val downloadingId by controller.downloadingId.collectAsState()
                            val actionMessage by controller.actionMessage.collectAsState()
                            DocumentWorkspace(
                                documents = docs,
                                viewer = DocumentViewerContext(DocumentViewerRole.STUDENT, deptId),
                                departments = emptyList(),
                                canManage = false,
                                loading = loading,
                                busy = busy,
                                downloadingId = downloadingId,
                                errorMessage = null,
                                actionMessage = actionMessage,
                                pickedFile = null,
                                fileSelectionError = null,
                                onRetry = controller::load,
                                onChooseFile = {},
                                onClearPickedFile = {},
                                onCreate = { _, _ -> },
                                onOpenFile = {},
                                onSetPublished = { _, _ -> },
                                onDelete = {},
                            )
                        }
                        StudentLeaf.Fees -> {
                            val controller = remember(component, sessionId) {
                                StudentFeeChallanController(sessionId, component.sessionFeeRepository(), scope)
                            }
                            val fee by controller.fee.collectAsState()
                            val loading by controller.loading.collectAsState()
                            StudentFeeWorkspace(
                                snapshot = if (loading && fee == null) null else studentFeeSnapshot(fee, LocalDate.now()),
                                loading = loading,
                                errorMessage = null,
                                onRetry = controller::refresh,
                            )
                        }
                        StudentLeaf.Notifications -> {
                            val controller = remember(component) {
                                NotificationsController(
                                    repository = component.notificationRepository(),
                                    viewerRole = NotificationTargetRole.STUDENT,
                                    accountKey = accountKey,
                                    sessionRepository = component.academicSessionRepository(),
                                    departmentRepository = component.departmentRepository(),
                                    publisherKind = NotificationPublisherKind.NONE,
                                    scope = scope,
                                )
                            }
                            NotificationControllerWorkspace(controller = controller)
                        }
                        StudentLeaf.Profile -> {
                            val controller = remember(component, sessionId, rollNumber) {
                                StudentProfileController(sessionId, rollNumber, component.academicSessionRepository(), component.fineRepository(), scope)
                            }
                            val profile by controller.profile.collectAsState()
                            val fines by controller.fines.collectAsState()
                            val loading by controller.loading.collectAsState()
                            var department by remember { mutableStateOf<com.mbd.cmscommon.domain.model.Department?>(null) }
                            androidx.compose.runtime.LaunchedEffect(deptId) {
                                department = runCatching { component.departmentRepository().getDepartment(deptId) }.getOrNull()
                            }
                            StudentOwnProfileWorkspace(
                                session = studentContext?.session,
                                studentName = studentContext?.name.orEmpty(),
                                rollNumber = rollNumber,
                                gpa = studentContext?.gpa,
                                cgpa = studentContext?.cgpa,
                                linkedEmail = accountKey,
                                profile = profile,
                                departmentName = department?.name,
                                accountKey = accountKey,
                                fines = fines,
                                loading = loading && studentContext == null,
                                errorMessage = null,
                                actionMessage = null,
                                onRetry = controller::refresh,
                                onResetPassword = { scope.launch { runCatching { component.sessionManager().sendPasswordReset(accountKey) } } },
                                onSignOut = onSignOut,
                            )
                        }
                    }
                }
            } else {
                when (tab) {
                    StudentTab.Home -> {
                        val controller = remember(component, sessionId, rollNumber) {
                            StudentHomeController(
                                sessionId,
                                rollNumber,
                                component.academicSessionRepository(),
                                component.sessionAttendanceRepository(),
                                component.sessionTimetableRepository(),
                                scope,
                            )
                        }
                        val ui by controller.ui.collectAsState()
                        val ctx = studentContext
                        StudentHomeWorkspace(
                            heroPainter = painterResource("splash_postgraduate_block.jpg"),
                            snapshot = ctx?.let {
                                studentHomeSnapshot(
                                    name = it.name,
                                    rollNumber = it.rollNumber,
                                    session = it.session,
                                    gpa = it.gpa,
                                    cgpa = it.cgpa,
                                    overallAttendance = ui.overallPercent,
                                    subjectCount = ui.subjectCount,
                                    lecturesToday = ui.lecturesToday,
                                    nextClass = ui.nextClass,
                                    weakestSubject = ui.weakestSubject,
                                )
                            },
                            loading = ctx == null,
                            onOpen = { destination ->
                                when (destination) {
                                    StudentHomeDestination.ATTENDANCE -> tab = StudentTab.Attendance
                                    StudentHomeDestination.MARKS -> leaf = StudentLeaf.Marks
                                    StudentHomeDestination.TIMETABLE -> tab = StudentTab.Timetable
                                    StudentHomeDestination.FEES -> leaf = StudentLeaf.Fees
                                }
                            },
                        )
                    }
                    StudentTab.Attendance -> {
                        val controller = remember(component, sessionId, rollNumber) {
                            StudentAttendanceController(sessionId, rollNumber, component.sessionAttendanceRepository(), component.curriculumRepository(), scope)
                        }
                        val rows by controller.rows.collectAsState()
                        StudentAttendanceWorkspace(
                            heroPainter = painterResource("splash_postgraduate_block.jpg"),
                            snapshot = if (rows.isEmpty()) null else studentAttendanceSnapshot(rows),
                            loading = rows.isEmpty(),
                        )
                    }
                    StudentTab.ExamsHub -> {
                        val controller = remember(component, sessionId, rollNumber) {
                            StudentExamsHubController(sessionId, rollNumber, component.sessionMarksRepository(), component.datesheetRepository(), scope)
                        }
                        val snapshot by controller.snapshot.collectAsState()
                        val loading by controller.loading.collectAsState()
                        StudentExamsHubWorkspace(
                            heroPainter = painterResource("splash_postgraduate_block.jpg"),
                            snapshot = snapshot,
                            loading = loading,
                            errorMessage = null,
                            onRetry = controller::refresh,
                            onOpen = { destination ->
                                when (destination) {
                                    StudentExamsDestination.MARKS -> leaf = StudentLeaf.Marks
                                    StudentExamsDestination.RESULTS -> leaf = StudentLeaf.Results
                                    StudentExamsDestination.DATESHEETS -> leaf = StudentLeaf.Datesheets
                                }
                            },
                        )
                    }
                    StudentTab.Timetable -> {
                        val controller = remember(component, sessionId) {
                            StudentTimetableController(sessionId, component.sessionTimetableRepository(), scope)
                        }
                        val periods by controller.periods.collectAsState()
                        val refreshing by controller.refreshing.collectAsState()
                        StudentTimetableWorkspace(
                            heroPainter = painterResource("splash_postgraduate_block.jpg"),
                            snapshot = studentTimetableSnapshot(periods, LocalDate.now(), java.time.LocalTime.now()),
                            loading = refreshing && periods.isEmpty(),
                            errorMessage = null,
                            onRetry = controller::refresh,
                        )
                    }
                    StudentTab.More -> {
                        val controller = remember(component, sessionId, deptId, rollNumber) {
                            StudentMoreController(
                                sessionId,
                                deptId,
                                rollNumber,
                                component.calendarRepository(),
                                component.documentRepository(),
                                component.sessionFeeRepository(),
                                component.notificationRepository(),
                                component.academicSessionRepository(),
                                scope,
                            )
                        }
                        val snapshot by controller.snapshot.collectAsState()
                        val loading by controller.loading.collectAsState()
                        val loadError by controller.loadError.collectAsState()
                        StudentMoreWorkspace(
                            heroPainter = painterResource("splash_postgraduate_block.jpg"),
                            snapshot = snapshot,
                            loading = loading,
                            errorMessage = loadError,
                            onRetry = controller::refresh,
                            onOpen = { destination ->
                                when (destination) {
                                    StudentMoreDestination.CALENDAR -> leaf = StudentLeaf.Events
                                    StudentMoreDestination.DOCUMENTS -> leaf = StudentLeaf.Documents
                                    StudentMoreDestination.FEES -> leaf = StudentLeaf.Fees
                                    StudentMoreDestination.NOTIFICATIONS -> leaf = StudentLeaf.Notifications
                                    StudentMoreDestination.PROFILE -> leaf = StudentLeaf.Profile
                                }
                            },
                            onSignOut = onSignOut,
                        )
                    }
                }
            }
        }
    }
}

private fun leafTitle(leaf: StudentLeaf): String = when (leaf) {
    StudentLeaf.Marks -> "Marks"
    StudentLeaf.Results -> "Results"
    StudentLeaf.Events -> "Events"
    StudentLeaf.Datesheets -> "Datesheets"
    StudentLeaf.Documents -> "Documents"
    StudentLeaf.Fees -> "Fee challan"
    StudentLeaf.Notifications -> "Notifications"
    StudentLeaf.Profile -> "Profile"
}
