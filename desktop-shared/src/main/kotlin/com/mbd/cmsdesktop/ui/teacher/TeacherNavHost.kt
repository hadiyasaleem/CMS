package com.mbd.cmsdesktop.ui.teacher

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.res.painterResource
import com.mbd.cmscommon.controller.NotificationPublisherKind
import com.mbd.cmscommon.domain.model.CalendarViewerContext
import com.mbd.cmscommon.domain.model.CalendarViewerRole
import com.mbd.cmscommon.domain.model.DatesheetViewerContext
import com.mbd.cmscommon.domain.model.DatesheetViewerRole
import com.mbd.cmscommon.domain.model.DocumentViewerContext
import com.mbd.cmscommon.domain.model.DocumentViewerRole
import com.mbd.cmscommon.domain.model.NotificationTargetRole
import com.mbd.cmscommon.domain.model.UserRole
import com.mbd.cmscommon.domain.model.teacherMenuSnapshot
import com.mbd.cmscommon.domain.repository.NotificationAudienceContext
import com.mbd.cmscommon.teacher.TeacherAssignmentsProvider
import com.mbd.cmscommon.ui.components.CmsTopBar
import com.mbd.cmscommon.ui.components.ExamsDestination
import com.mbd.cmscommon.ui.components.InsightsViewer
import com.mbd.cmscommon.ui.components.TeacherMenuWorkspace
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmsdesktop.di.DesktopAppComponent
import com.mbd.cmsdesktop.ui.admin.CalendarScreen
import com.mbd.cmsdesktop.ui.admin.LinkRequestsScreen
import com.mbd.cmsdesktop.ui.parity.desktopBackHandler
import com.mbd.cmsdesktop.ui.shared.DatesheetsScreen
import com.mbd.cmsdesktop.ui.shared.DocumentsScreen
import com.mbd.cmsdesktop.ui.shared.InsightsScreen
import com.mbd.cmsdesktop.ui.shared.NotificationsScreen
import java.awt.event.WindowEvent
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Top-level shell for the teacher desktop app: a [CmsTopBar] + screen body + bottom
 * [NavigationBar] over the 5 mobile-parity [TeacherTab]s. Navigation state is a single
 * `screen: TeacherScreen` that tabs reset to their root and leaves push into; the back gesture
 * (window close / Escape via [desktopBackHandler]) returns to the current tab's root before it
 * closes the window.
 */
@Composable
fun TeacherNavHost(role: UserRole.Teacher, component: DesktopAppComponent, window: ComposeWindow, onSignOut: () -> Unit) {
    var selectedTab by remember { mutableStateOf(TeacherTab.Home) }
    var screen by remember { mutableStateOf<TeacherScreen>(TeacherTab.Home.root) }
    val refreshScope = rememberCoroutineScope()
    var shellRefreshing by remember { mutableStateOf(false) }
    var refreshVersion by remember { mutableIntStateOf(0) }

    val notificationRepository = component.notificationRepository()
    var notificationContext by remember { mutableStateOf(NotificationAudienceContext()) }
    val unreadCount by remember(notificationRepository, notificationContext) {
        notificationRepository.observeUnreadCount(NotificationTargetRole.TEACHER, notificationContext)
    }.collectAsState(initial = 0)

    val assignmentsProvider = remember(component) {
        TeacherAssignmentsProvider(
            component.sessionManager(),
            component.sessionTimetableRepository(),
            component.academicSessionRepository(),
            component.departmentRepository(),
        )
    }
    val teacherId = role.teacherId

    LaunchedEffect(teacherId) {
        val teacher = component.teacherRepository().getTeacher(teacherId)
        notificationContext = NotificationAudienceContext(departmentId = teacher?.deptId)
        notificationRepository.sync(NotificationTargetRole.TEACHER, notificationContext)
    }

    fun refreshCurrentScreen() {
        if (shellRefreshing) return
        refreshScope.launch {
            shellRefreshing = true
            runCatching { component.adminDataBootstrapper().refreshAll() }
            refreshVersion++
            shellRefreshing = false
        }
    }

    fun goTab(tab: TeacherTab) {
        selectedTab = tab
        screen = tab.root
    }

    Column(Modifier.fillMaxSize()) {
        CmsTopBar(
            onBack = {
                if (screen == selectedTab.root) {
                    window.dispatchEvent(WindowEvent(window, WindowEvent.WINDOW_CLOSING))
                } else {
                    screen = selectedTab.root
                }
            },
            onRefresh = ::refreshCurrentScreen,
            isRefreshing = shellRefreshing,
            onNotifications = { screen = TeacherScreen.Notifications },
            notificationCount = unreadCount,
            goldWordmark = true,
        )
        Box(
            Modifier
                .weight(1f)
                .fillMaxSize()
                .desktopBackHandler(screen != selectedTab.root) {
                    if (screen == selectedTab.root) {
                        window.dispatchEvent(WindowEvent(window, WindowEvent.WINDOW_CLOSING))
                    } else {
                        screen = selectedTab.root
                    }
                },
        ) {
            // keyed on (screen, refreshVersion) so pull-to-refresh forces a fresh screen instance
            key(screen, refreshVersion) {
            when (val currentScreen = screen) {
                TeacherScreen.Home -> HomeScreen(
                    role = role,
                    timetableRepository = component.sessionTimetableRepository(),
                    assignmentsProvider = assignmentsProvider,
                    onNavigate = { screen = it },
                )

                TeacherScreen.Attendance -> MarkAttendanceScreen(
                    teacherId = teacherId,
                    sessionRepository = component.academicSessionRepository(),
                    attendanceRepository = component.sessionAttendanceRepository(),
                    notificationRepository = component.notificationRepository(),
                    assignmentsProvider = assignmentsProvider,
                    onOpenHistory = { sessionId, courseCode -> screen = TeacherScreen.AttendanceHistory(sessionId, courseCode) },
                )

                is TeacherScreen.AttendanceHistory -> AttendanceHistoryScreen(
                    sessionId = currentScreen.sessionId,
                    courseCode = currentScreen.courseCode,
                    sessionRepository = component.academicSessionRepository(),
                    attendanceRepository = component.sessionAttendanceRepository(),
                    window = window,
                )

                TeacherScreen.ExamsHub -> ExamsHubScreen(
                    teacherId = teacherId,
                    assignmentsProvider = assignmentsProvider,
                    examPaperRepository = component.examPaperRepository(),
                    datesheetRepository = component.datesheetRepository(),
                    onOpen = { destination ->
                        screen = when (destination) {
                            ExamsDestination.MARKS -> TeacherScreen.Marks
                            ExamsDestination.EXAM_PAPER -> TeacherScreen.ExamPaper
                            ExamsDestination.RESULTS -> TeacherScreen.SemesterResults
                            ExamsDestination.DATESHEETS -> TeacherScreen.Datesheets
                        }
                    },
                )

                TeacherScreen.Marks -> MarksEntryScreen(
                    teacherId = teacherId,
                    sessionRepository = component.academicSessionRepository(),
                    marksRepository = component.sessionMarksRepository(),
                    markEditRequestRepository = component.markEditRequestRepository(),
                    assignmentsProvider = assignmentsProvider,
                )

                TeacherScreen.ExamPaper -> ExamPaperSubmissionScreen(
                    teacherId = teacherId,
                    examPaperRepository = component.examPaperRepository(),
                    assignmentsProvider = assignmentsProvider,
                    window = window,
                )

                TeacherScreen.SemesterResults -> SemesterResultsScreen(
                    teacherId = teacherId,
                    sessionRepository = component.academicSessionRepository(),
                    marksRepository = component.sessionMarksRepository(),
                    curriculumRepository = component.curriculumRepository(),
                    assignmentsProvider = assignmentsProvider,
                )

                TeacherScreen.Schedule -> ScheduleScreen(
                    teacherId = teacherId,
                    departmentRepository = component.departmentRepository(),
                    sessionRepository = component.academicSessionRepository(),
                    timetableRepository = component.sessionTimetableRepository(),
                )

                TeacherScreen.MenuHub -> {
                    val teacherProfile by component.teacherRepository().observeTeacher(teacherId).collectAsState(initial = null)
                    val menuAssignments by assignmentsProvider.observeAssignmentsFor(teacherId).collectAsState(initial = emptyList())
                    val pendingRequests by component.studentLinkRequestRepository().observePendingRequests().collectAsState(initial = emptyList())
                    val snapshot = teacherMenuSnapshot(teacherProfile, menuAssignments, unreadCount, pendingRequests.size)

                    TeacherMenuWorkspace(
                        heroPainter = painterResource("teacher-menu-hero.jpg"),
                        snapshot = snapshot,
                        onOpenMyStudents = { screen = TeacherScreen.MyStudents },
                        onOpenCalendar = { screen = TeacherScreen.Calendar },
                        onOpenDocuments = { screen = TeacherScreen.Documents },
                        onOpenInsights = { screen = TeacherScreen.Insights },
                        onOpenLinkRequests = { screen = TeacherScreen.LinkRequests },
                        onOpenNotifications = { screen = TeacherScreen.Notifications },
                        onOpenProfile = { screen = TeacherScreen.Profile },
                        onSignOut = onSignOut,
                    )
                }

                TeacherScreen.MyStudents -> MyStudentsScreen(
                    teacherId = teacherId,
                    sessionRepository = component.academicSessionRepository(),
                    attendanceRepository = component.sessionAttendanceRepository(),
                    timetableRepository = component.sessionTimetableRepository(),
                    assignmentsProvider = assignmentsProvider,
                )

                TeacherScreen.Calendar -> {
                    val teacherProfile by component.teacherRepository().observeTeacher(teacherId).collectAsState(initial = null)
                    val calendarAssignments by assignmentsProvider.observeAssignmentsFor(teacherId).collectAsState(initial = emptyList())
                    CalendarScreen(
                        repository = component.calendarRepository(),
                        departmentRepository = component.departmentRepository(),
                        sessionRepository = component.academicSessionRepository(),
                        createdBy = component.sessionManager().accountKey,
                        viewer = CalendarViewerContext(
                            role = CalendarViewerRole.TEACHER,
                            departmentId = teacherProfile?.deptId,
                            sessionIds = calendarAssignments.mapTo(mutableSetOf()) { it.sessionId },
                        ),
                        canEdit = false,
                    )
                }

                TeacherScreen.Datesheets -> {
                    val signedInTeacher by component.teacherRepository().observeTeacher(teacherId).collectAsState(initial = null)
                    DatesheetsScreen(
                        repository = component.datesheetRepository(),
                        sessionRepository = component.academicSessionRepository(),
                        curriculumRepository = component.curriculumRepository(),
                        viewer = DatesheetViewerContext(
                            role = DatesheetViewerRole.TEACHER,
                            canManage = role.permissions.canManageDatesheets,
                            identityKey = teacherId,
                        ),
                        invigilators = listOfNotNull(signedInTeacher),
                        createdBy = component.sessionManager().accountKey,
                    )
                }

                TeacherScreen.Documents -> {
                    val teacherProfile by component.teacherRepository().observeTeacher(teacherId).collectAsState(initial = null)
                    DocumentsScreen(
                        repository = component.documentRepository(),
                        departmentRepository = component.departmentRepository(),
                        viewer = DocumentViewerContext(DocumentViewerRole.TEACHER, teacherProfile?.deptId),
                        canUpload = false,
                        window = window,
                        publishedBy = component.sessionManager().accountKey,
                    )
                }

                TeacherScreen.Insights -> {
                    val insightAssignments by assignmentsProvider.observeAssignmentsFor(teacherId).collectAsState(initial = emptyList())
                    InsightsScreen(
                        repository = component.insightsRepository(),
                        sessionRepository = component.academicSessionRepository(),
                        departmentRepository = component.departmentRepository(),
                        viewer = InsightsViewer.TEACHER,
                        assignments = insightAssignments,
                    )
                }

                TeacherScreen.LinkRequests -> {
                    val teacherIdForApproval = component.sessionManager().accountKey.orEmpty()
                    LinkRequestsScreen(
                        repository = component.studentLinkRequestRepository(),
                        sessionRepository = component.academicSessionRepository(),
                        departmentRepository = component.departmentRepository(),
                        reviewedBy = teacherIdForApproval,
                        permissionCheck = {
                            component.teacherRepository().getTeacher(teacherIdForApproval)?.permissions?.canApproveLinkRequests == true
                        },
                    )
                }

                TeacherScreen.Notifications -> {
                    val teacherId2 = component.sessionManager().accountKey.orEmpty()
                    val audienceContext = remember(teacherId2) {
                        component.teacherRepository().observeTeacher(teacherId2)
                            .map { NotificationAudienceContext(departmentId = it?.deptId) }
                    }
                    val permissionCheck: suspend () -> Boolean = remember(teacherId2) {
                        { component.teacherRepository().getTeacher(teacherId2)?.permissions?.canSendNotifications == true }
                    }
                    val notificationAssignments = remember(teacherId2) { assignmentsProvider.observeAssignmentsFor(teacherId2) }
                    NotificationsScreen(
                        repository = component.notificationRepository(),
                        role = NotificationTargetRole.TEACHER,
                        accountKey = teacherId2,
                        sessionRepository = component.academicSessionRepository(),
                        departmentRepository = component.departmentRepository(),
                        publisherKind = NotificationPublisherKind.TEACHER,
                        audienceContext = audienceContext,
                        hasAssignmentsCheck = permissionCheck,
                        assignmentsFlow = notificationAssignments,
                    )
                }

                TeacherScreen.Profile -> TeacherProfileScreen(
                    teacherId = teacherId,
                    sessionManager = component.sessionManager(),
                    teacherRepository = component.teacherRepository(),
                    departmentRepository = component.departmentRepository(),
                    assignmentsProvider = assignmentsProvider,
                    onSignOut = onSignOut,
                )
            }
            }
        }
        NavigationBar(containerColor = CmsTheme.colors.ink, contentColor = CmsTheme.colors.onInk) {
            TeacherTab.entries.forEach { tab ->
                NavigationBarItem(
                    selected = selectedTab == tab,
                    onClick = { goTab(tab) },
                    icon = { Icon(tab.icon, contentDescription = tab.label) },
                    label = { Text(tab.label) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CmsTheme.colors.onInk,
                        selectedTextColor = CmsTheme.colors.onInk,
                        indicatorColor = CmsTheme.colors.accent,
                        unselectedIconColor = CmsTheme.colors.onInkMuted,
                        unselectedTextColor = CmsTheme.colors.onInkMuted,
                    ),
                )
            }
        }
    }
}
