package com.mbd.cmsdesktop.ui.admin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.controller.NotificationPublisherKind
import com.mbd.cmscommon.controller.NotificationsController
import com.mbd.cmscommon.domain.model.CalendarViewerContext
import com.mbd.cmscommon.domain.model.CalendarViewerRole
import com.mbd.cmscommon.domain.model.DatesheetViewerContext
import com.mbd.cmscommon.domain.model.DatesheetViewerRole
import com.mbd.cmscommon.domain.model.NotificationTargetRole
import com.mbd.cmscommon.domain.model.UserRole
import com.mbd.cmscommon.teacher.TeacherAssignmentsProvider
import com.mbd.cmscommon.ui.components.CmsTopBar
import com.mbd.cmscommon.ui.components.InsightsViewer
import com.mbd.cmscommon.ui.components.MoreDestination
import com.mbd.cmscommon.ui.components.NotificationControllerWorkspace
import com.mbd.cmscommon.ui.components.PeopleDestination
import com.mbd.cmscommon.ui.components.RecordsDestination
import com.mbd.cmscommon.ui.components.RefreshBox
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmsdesktop.di.DesktopAppComponent
import com.mbd.cmsdesktop.ui.parity.desktopBackHandler
import com.mbd.cmsdesktop.ui.shared.DatesheetsScreen
import com.mbd.cmsdesktop.ui.shared.InsightsScreen
import com.mbd.cmsdesktop.ui.shared.NotificationsScreen
import kotlinx.coroutines.launch
import java.awt.event.WindowEvent

/**
 * Root shell for the admin desktop app: a mobile-parity [NavigationBar] of the 5 [AdminTab]s, each rooted
 * at an [AdminScreen], plus a manually-kept backstack for the drill-down chain
 * (department -> session -> semester/timetable/fees -> student profile). There is no Compose
 * Navigation dependency here, matching the rest of this codebase's manual-controller-construction
 * pattern - the decompiled original kept the same hand-rolled `SnapshotStateList<AdminScreen>`.
 */
@Composable
fun AdminNavHost(role: UserRole.Admin, component: DesktopAppComponent, window: ComposeWindow, onSignOut: () -> Unit) {
    val scope = rememberCoroutineScope()
    val refreshScope = rememberCoroutineScope()
    val accountKey = component.sessionManager().accountKey.orEmpty()

    var selectedTab by remember { mutableStateOf(AdminTab.Dashboard) }
    val backStack = remember { mutableStateListOf<AdminScreen>(AdminTab.Dashboard.root) }
    val screen by remember { derivedStateOf { backStack.last() } }

    var shellRefreshing by remember { mutableStateOf(false) }
    var refreshVersion by remember { mutableStateOf(0) }

    val notificationRepository = remember(component) { component.notificationRepository() }
    val unreadCount by remember(notificationRepository) {
        notificationRepository.observeUnreadCount(NotificationTargetRole.ADMIN)
    }.collectAsState(initial = 0)


    fun push(destination: AdminScreen) {
        backStack.add(destination)
    }

    fun popOrSwitchTab(tab: AdminTab) {
        selectedTab = tab
        backStack.clear()
        backStack.add(tab.root)
    }

    fun refreshShell() {
        refreshScope.launch {
            shellRefreshing = true
            try {
                component.adminDataBootstrapper().refreshAll()
                refreshVersion++
            } finally {
                shellRefreshing = false
            }
        }
    }

    val teacherAssignmentsProvider = remember(component) {
        TeacherAssignmentsProvider(component.sessionManager(), component.sessionTimetableRepository(), component.academicSessionRepository(), component.departmentRepository())
    }

    Scaffold(
        topBar = {
            CmsTopBar(
                title = "GGC-MBD",
                onBack = {
                    if (backStack.size > 1) {
                        backStack.removeAt(backStack.lastIndex)
                    } else {
                        window.dispatchEvent(WindowEvent(window, WindowEvent.WINDOW_CLOSING))
                    }
                },
                onRefresh = ::refreshShell,
                isRefreshing = shellRefreshing,
                onNotifications = { push(AdminScreen.Notifications) },
                notificationCount = unreadCount,
            )
        },
        bottomBar = {
            NavigationBar(containerColor = CmsTheme.colors.ink, contentColor = CmsTheme.colors.onInk) {
                AdminTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab && backStack.size == 1,
                        onClick = { popOrSwitchTab(tab) },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CmsTheme.colors.onInk,
                            selectedTextColor = CmsTheme.colors.onInk,
                            unselectedIconColor = CmsTheme.colors.onInkMuted,
                            unselectedTextColor = CmsTheme.colors.onInkMuted,
                            indicatorColor = CmsTheme.colors.accent,
                        ),
                    )
                }
            }
        },
    ) { padding ->
        RefreshBox(
            isRefreshing = shellRefreshing,
            onRefresh = ::refreshShell,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .desktopBackHandler(enabled = backStack.size > 1) { backStack.removeAt(backStack.lastIndex) },
        ) {
            Box(Modifier.fillMaxSize()) {
                when (val current = screen) {
                    AdminScreen.Dashboard -> DashboardScreen(
                        departmentRepository = component.departmentRepository(),
                        teacherRepository = component.teacherRepository(),
                        sessionRepository = component.academicSessionRepository(),
                        linkRequestRepository = component.studentLinkRequestRepository(),
                        onOpenAcademics = { popOrSwitchTab(AdminTab.Academics) },
                        onOpenTeachers = { push(AdminScreen.Teachers) },
                        onOpenCalendar = { push(AdminScreen.Calendar) },
                        onOpenInsights = { push(AdminScreen.Insights) },
                        onOpenLinkRequests = { push(AdminScreen.LinkRequests) },
                        onOpenMasterTimetable = { push(AdminScreen.MasterTimetable) },
                        onOpenNotifications = { push(AdminScreen.Notifications) },
                    )

                    AdminScreen.Academics -> DepartmentsScreen(
                        repository = component.departmentRepository(),
                        sessionRepository = component.academicSessionRepository(),
                        teacherRepository = component.teacherRepository(),
                        createdBy = accountKey,
                        onOpenDepartment = { deptId -> push(AdminScreen.DeptDetail(deptId)) },
                    )

                    AdminScreen.PeopleHub -> PeopleHubScreen(
                        administratorRepository = component.administratorRepository(),
                        teacherRepository = component.teacherRepository(),
                        sessionRepository = component.academicSessionRepository(),
                        linkRequestRepository = component.studentLinkRequestRepository(),
                        markEditRequestRepository = component.markEditRequestRepository(),
                        onOpen = { destination ->
                            when (destination) {
                                PeopleDestination.ADMINISTRATORS -> push(AdminScreen.Administrators)
                                PeopleDestination.TEACHERS -> push(AdminScreen.Teachers)
                                PeopleDestination.STUDENTS -> popOrSwitchTab(AdminTab.Academics)
                                PeopleDestination.LINK_REQUESTS -> push(AdminScreen.LinkRequests)
                                PeopleDestination.MARK_EDIT_REQUESTS -> push(AdminScreen.MarkEditRequests)
                            }
                        },
                    )

                    AdminScreen.RecordsHub -> RecordsHubScreen(
                        sessionRepository = component.academicSessionRepository(),
                        calendarRepository = component.calendarRepository(),
                        datesheetRepository = component.datesheetRepository(),
                        insightsRepository = component.insightsRepository(),
                        onOpen = { destination ->
                            when (destination) {
                                RecordsDestination.ATTENDANCE -> push(AdminScreen.AttendanceRecords)
                                RecordsDestination.CALENDAR -> push(AdminScreen.Calendar)
                                RecordsDestination.DATESHEETS -> push(AdminScreen.Datesheets)
                                RecordsDestination.TIMETABLE -> push(AdminScreen.MasterTimetable)
                                RecordsDestination.FEES -> push(AdminScreen.FeesPicker)
                                RecordsDestination.INSIGHTS -> push(AdminScreen.Insights)
                            }
                        },
                    )

                    AdminScreen.MoreHub -> MoreHubScreen(
                        sessionManager = component.sessionManager(),
                        administratorRepository = component.administratorRepository(),
                        notificationRepository = component.notificationRepository(),
                        onOpen = { destination ->
                            when (destination) {
                                MoreDestination.NOTIFICATIONS -> push(AdminScreen.Notifications)
                                MoreDestination.PROFILE -> push(AdminScreen.Profile)
                            }
                        },
                    )

                    AdminScreen.Administrators -> AdministratorsScreen(
                        repository = component.administratorRepository(),
                        currentAccountKey = accountKey,
                    )

                    AdminScreen.Teachers -> TeachersScreen(
                        repository = component.teacherRepository(),
                        departmentRepository = component.departmentRepository(),
                        createdBy = accountKey,
                        assignmentsProvider = teacherAssignmentsProvider,
                    )

                    AdminScreen.LinkRequests -> LinkRequestsScreen(
                        repository = component.studentLinkRequestRepository(),
                        sessionRepository = component.academicSessionRepository(),
                        departmentRepository = component.departmentRepository(),
                        reviewedBy = accountKey,
                    )

                    AdminScreen.MarkEditRequests -> MarkEditRequestsScreen(
                        repository = component.markEditRequestRepository(),
                        sessionRepository = component.academicSessionRepository(),
                        curriculumRepository = component.curriculumRepository(),
                        departmentRepository = component.departmentRepository(),
                        teacherRepository = component.teacherRepository(),
                        reviewedBy = accountKey,
                    )

                    AdminScreen.AttendanceRecords -> AttendanceRecordsScreen(
                        departmentRepository = component.departmentRepository(),
                        sessionRepository = component.academicSessionRepository(),
                        attendanceRepository = component.sessionAttendanceRepository(),
                        curriculumRepository = component.curriculumRepository(),
                        window = window,
                    )

                    AdminScreen.Calendar -> CalendarScreen(
                        repository = component.calendarRepository(),
                        departmentRepository = component.departmentRepository(),
                        sessionRepository = component.academicSessionRepository(),
                        createdBy = accountKey,
                        viewer = CalendarViewerContext(role = CalendarViewerRole.ADMIN),
                    )

                    AdminScreen.Datesheets -> DatesheetsScreen(
                        repository = component.datesheetRepository(),
                        sessionRepository = component.academicSessionRepository(),
                        curriculumRepository = component.curriculumRepository(),
                        viewer = DatesheetViewerContext(role = DatesheetViewerRole.ADMIN, canManage = true),
                        createdBy = accountKey,
                    )

                    AdminScreen.MasterTimetable -> MasterTimetableScreen(
                        departmentRepository = component.departmentRepository(),
                        sessionRepository = component.academicSessionRepository(),
                        timetableRepository = component.sessionTimetableRepository(),
                        onOpenSession = { sessionId -> push(AdminScreen.SessionDetail(sessionId)) },
                    )

                    AdminScreen.FeesPicker -> DepartmentsScreen(
                        repository = component.departmentRepository(),
                        sessionRepository = component.academicSessionRepository(),
                        teacherRepository = component.teacherRepository(),
                        createdBy = accountKey,
                        onOpenDepartment = { deptId -> push(AdminScreen.DeptDetail(deptId)) },
                    )

                    AdminScreen.Insights -> InsightsScreen(
                        repository = component.insightsRepository(),
                        sessionRepository = component.academicSessionRepository(),
                        departmentRepository = component.departmentRepository(),
                        viewer = InsightsViewer.ADMIN,
                    )

                    AdminScreen.Notifications -> {
                        val controller = remember(component) {
                            NotificationsController(
                                repository = component.notificationRepository(),
                                viewerRole = NotificationTargetRole.ADMIN,
                                accountKey = accountKey,
                                sessionRepository = component.academicSessionRepository(),
                                departmentRepository = component.departmentRepository(),
                                publisherKind = NotificationPublisherKind.ADMIN,
                                scope = scope,
                            )
                        }
                        NotificationControllerWorkspace(controller = controller)
                    }

                    AdminScreen.Profile -> AdminProfileScreen(
                        sessionManager = component.sessionManager(),
                        repository = component.administratorRepository(),
                        onSignOut = onSignOut,
                    )

                    is AdminScreen.DeptDetail -> DepartmentDetailScreen(
                        deptId = current.deptId,
                        departmentRepository = component.departmentRepository(),
                        sessionRepository = component.academicSessionRepository(),
                        teacherRepository = component.teacherRepository(),
                        editedBy = accountKey,
                        onOpenSession = { sessionId -> push(AdminScreen.SessionDetail(sessionId)) },
                    )

                    is AdminScreen.SessionDetail -> SessionDetailScreen(
                        sessionId = current.sessionId,
                        sessionRepository = component.academicSessionRepository(),
                        curriculumRepository = component.curriculumRepository(),
                        timetableRepository = component.sessionTimetableRepository(),
                        feeRepository = component.sessionFeeRepository(),
                        teacherRepository = component.teacherRepository(),
                        onOpenStudents = { sessionId -> push(AdminScreen.SessionStudents(sessionId)) },
                        onOpenTimetable = { sessionId -> push(AdminScreen.SessionTimetableRoute(sessionId)) },
                        onOpenSemester = { sessionId, semester -> push(AdminScreen.SemesterSubjectsRoute(sessionId, semester)) },
                        onOpenFees = { sessionId -> push(AdminScreen.SessionFeesRoute(sessionId)) },
                        onDeleted = { backStack.removeAt(backStack.lastIndex) },
                    )

                    is AdminScreen.SessionStudents -> SessionStudentsScreen(
                        sessionId = current.sessionId,
                        sessionRepository = component.academicSessionRepository(),
                        window = window,
                        onOpenStudent = { sessionId, roll -> push(AdminScreen.StudentProfile(sessionId, roll)) },
                    )

                    is AdminScreen.StudentProfile -> StudentProfileScreen(
                        sessionId = current.sessionId,
                        rollNumber = current.roll,
                        sessionRepository = component.academicSessionRepository(),
                        fineRepository = component.fineRepository(),
                        sessionManager = component.sessionManager(),
                    )

                    is AdminScreen.SessionTimetableRoute -> SessionTimetableScreen(
                        sessionId = current.sessionId,
                        sessionRepository = component.academicSessionRepository(),
                        curriculumRepository = component.curriculumRepository(),
                        teacherRepository = component.teacherRepository(),
                        timetableRepository = component.sessionTimetableRepository(),
                    )

                    is AdminScreen.SemesterSubjectsRoute -> SemesterSubjectsScreen(
                        sessionId = current.sessionId,
                        semester = current.semester,
                        curriculumRepository = component.curriculumRepository(),
                        sessionRepository = component.academicSessionRepository(),
                    )

                    is AdminScreen.SessionFeesRoute -> SessionFeesScreen(
                        sessionId = current.sessionId,
                        feeRepository = component.sessionFeeRepository(),
                        sessionRepository = component.academicSessionRepository(),
                        updatedBy = accountKey,
                    )
                }
            }
        }
    }
}
