package com.mbd.cmsdesktop.ui.student

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.domain.model.DatesheetViewerContext
import com.mbd.cmscommon.domain.model.DatesheetViewerRole
import com.mbd.cmscommon.domain.model.NotificationTargetRole
import com.mbd.cmscommon.domain.model.UserRole
import com.mbd.cmscommon.ui.components.CmsTopBar
import com.mbd.cmscommon.ui.components.NotificationBadge
import com.mbd.cmscommon.ui.components.StudentExamsDestination
import com.mbd.cmscommon.ui.components.StudentHomeDestination
import com.mbd.cmscommon.ui.components.StudentMoreDestination
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmsdesktop.di.DesktopAppComponent
import com.mbd.cmsdesktop.ui.shared.DatesheetsScreen
import com.mbd.cmsdesktop.ui.shared.NotificationsScreen
import com.mbd.cmscommon.util.StudentIdCodec
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Top-level shell for the student desktop app once a role is known: [UserRole.LinkedStudent] gets
 * the full 5-tab shell below; [UserRole.UnlinkedStudent] gets [StudentLinkRequestScreen] instead
 * (desktop's own account-linking flow - see that file's doc comment). [onRoleChanged] fires once an
 * unlinked account gets approved, so the caller (`Main.kt`) can swap this composable's `role` input
 * without a full re-login.
 *
 * Uses the shared [DatesheetsScreen]/[NotificationsScreen] composables from
 * `ui.shared` for the Datesheets/Notifications leaves instead of inlining controller +
 * workspace wiring per leaf (that inlining is what the earlier stopgap version of this file did).
 */
@Composable
fun StudentNavHost(role: UserRole, component: DesktopAppComponent, window: ComposeWindow, onSignOut: () -> Unit, onRoleChanged: (UserRole) -> Unit) {
    when (role) {
        is UserRole.LinkedStudent -> StudentShell(role, component, window, onSignOut)
        is UserRole.UnlinkedStudent -> StudentLinkRequestScreen(component, onLinked = onRoleChanged)
        else -> Text("This account is not a student account.", modifier = Modifier.padding(24.dp))
    }
}

@Composable
private fun StudentShell(role: UserRole.LinkedStudent, component: DesktopAppComponent, window: ComposeWindow, onSignOut: () -> Unit) {
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(StudentTab.Home) }
    var screen by remember { mutableStateOf<StudentScreen>(StudentTab.Home.root) }
    var shellRefreshing by remember { mutableStateOf(false) }
    var refreshVersion by remember { mutableIntStateOf(0) }

    val accountKey = component.sessionManager().accountKey.orEmpty()
    val sessionId = StudentIdCodec.sessionIdOf(role.studentId)
    val deptId = StudentIdCodec.deptIdOf(sessionId)
    val rollNumber = StudentIdCodec.rollOf(role.studentId)

    val unreadCount by component.notificationRepository()
        .observeUnreadCount(NotificationTargetRole.STUDENT)
        .collectAsState(initial = 0)

    fun refreshCurrentScreen() {
        scope.launch {
            shellRefreshing = true
            refreshVersion += 1
            delay(400)
            shellRefreshing = false
        }
    }

    fun open(target: StudentScreen) {
        screen = target
    }

    Column(Modifier.fillMaxSize()) {
        CmsTopBar(
            onBack = if (screen != selectedTab.root) {
                { screen = selectedTab.root }
            } else {
                null
            },
            onRefresh = ::refreshCurrentScreen,
            isRefreshing = shellRefreshing,
            onNotifications = { open(StudentScreen.Notifications) },
            notificationCount = unreadCount,
            goldWordmark = true,
        )
        Row(Modifier.weight(1f).fillMaxWidth()) {
            NavigationRail(containerColor = CmsTheme.colors.ink, contentColor = CmsTheme.colors.onInk) {
                Spacer(Modifier.height(16.dp))
                StudentTab.entries.forEach { tab ->
                    NavigationRailItem(
                        selected = screen == tab.root,
                        onClick = { selectedTab = tab; screen = tab.root },
                        icon = {
                            if (tab == StudentTab.More && unreadCount > 0) {
                                Box {
                                    Icon(tab.icon, contentDescription = tab.label)
                                    NotificationBadge(unreadCount, modifier = Modifier.align(Alignment.TopEnd))
                                }
                            } else {
                                Icon(tab.icon, contentDescription = tab.label)
                            }
                        },
                        label = { Text(tab.label) },
                    )
                }
            }
            Box(Modifier.weight(1f).fillMaxHeight().padding(horizontal = 24.dp)) {
                key(refreshVersion) {
                    when (screen) {
                        StudentScreen.Home -> StudentHomeWorkspaceScreen(
                            sessionId, rollNumber, component.academicSessionRepository(), component.sessionAttendanceRepository(), component.sessionTimetableRepository(),
                        ) { destination ->
                            when (destination) {
                                StudentHomeDestination.ATTENDANCE -> { selectedTab = StudentTab.Attendance; open(StudentScreen.Attendance) }
                                StudentHomeDestination.MARKS -> open(StudentScreen.Marks)
                                StudentHomeDestination.TIMETABLE -> { selectedTab = StudentTab.Timetable; open(StudentScreen.Timetable) }
                                StudentHomeDestination.FEES -> open(StudentScreen.Fees)
                            }
                        }
                        StudentScreen.Attendance -> StudentAttendanceWorkspaceScreen(
                            sessionId, rollNumber, component.sessionAttendanceRepository(), component.curriculumRepository(),
                        )
                        StudentScreen.ExamsHub -> StudentExamsHubScreen(
                            sessionId, rollNumber, component.sessionMarksRepository(), component.datesheetRepository(),
                        ) { destination ->
                            when (destination) {
                                StudentExamsDestination.MARKS -> open(StudentScreen.Marks)
                                StudentExamsDestination.RESULTS -> open(StudentScreen.Results)
                                StudentExamsDestination.DATESHEETS -> open(StudentScreen.Datesheets)
                            }
                        }
                        StudentScreen.Timetable -> StudentTimetableScreen(sessionId, component.sessionTimetableRepository())
                        StudentScreen.MoreHub -> StudentMoreScreen(
                            sessionId, deptId, rollNumber,
                            component.calendarRepository(), component.sessionFeeRepository(),
                            component.notificationRepository(), component.academicSessionRepository(),
                            onOpen = { destination ->
                                when (destination) {
                                    StudentMoreDestination.CALENDAR -> open(StudentScreen.Events)
                                    StudentMoreDestination.FEES -> open(StudentScreen.Fees)
                                    StudentMoreDestination.NOTIFICATIONS -> open(StudentScreen.Notifications)
                                    StudentMoreDestination.PROFILE -> open(StudentScreen.Profile)
                                }
                            },
                            onSignOut = onSignOut,
                        )
                        StudentScreen.Marks -> StudentMarksScreen(sessionId, rollNumber, component.sessionMarksRepository(), component.curriculumRepository())
                        StudentScreen.Results -> StudentResultsScreen(sessionId, rollNumber, component.sessionMarksRepository())
                        StudentScreen.Datesheets -> DatesheetsScreen(
                            repository = component.datesheetRepository(),
                            sessionRepository = component.academicSessionRepository(),
                            curriculumRepository = component.curriculumRepository(),
                            viewer = DatesheetViewerContext(role = DatesheetViewerRole.STUDENT, sessionId = sessionId, canManage = false),
                        )
                        StudentScreen.Events -> StudentCalendarScreen(sessionId, deptId, component.calendarRepository(), component.departmentRepository(), component.academicSessionRepository())
                        StudentScreen.Fees -> StudentFeeChallanScreen(sessionId, rollNumber, component.sessionFeeRepository())
                        StudentScreen.Notifications -> NotificationsScreen(
                            repository = component.notificationRepository(),
                            role = NotificationTargetRole.STUDENT,
                            accountKey = accountKey,
                            sessionRepository = component.academicSessionRepository(),
                            departmentRepository = component.departmentRepository(),
                        )
                        StudentScreen.Profile -> StudentOwnProfileScreen(
                            sessionId, rollNumber, component.sessionManager(), component.academicSessionRepository(), component.departmentRepository(), component.fineRepository(),
                            onSignOut = onSignOut,
                        )
                    }
                }
            }
        }
    }
}
