package com.mbd.cmsdesktop.ui.admin

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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.School
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
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.controller.AdministratorsController
import com.mbd.cmscommon.controller.DashboardController
import com.mbd.cmscommon.controller.DepartmentsActionController
import com.mbd.cmscommon.controller.LinkRequestsController
import com.mbd.cmscommon.controller.MarkEditRequestsController
import com.mbd.cmscommon.controller.MasterTimetableController
import com.mbd.cmscommon.controller.MoreHubController
import com.mbd.cmscommon.controller.PeopleHubController
import com.mbd.cmscommon.controller.RecordsHubController
import com.mbd.cmscommon.controller.departmentPortfolioStats
import com.mbd.cmscommon.domain.model.UserRole
import com.mbd.cmscommon.ui.components.AdminDashboardContent
import com.mbd.cmscommon.ui.components.AdministratorDirectoryWorkspace
import com.mbd.cmscommon.ui.components.AdministratorProfileWorkspace
import com.mbd.cmscommon.ui.components.DashboardActionUi
import com.mbd.cmscommon.ui.components.DepartmentPortfolio
import com.mbd.cmscommon.ui.components.LinkRequestReviewWorkspace
import com.mbd.cmscommon.ui.components.MarkEditRequestReviewWorkspace
import com.mbd.cmscommon.ui.components.MasterTimetableWorkspace
import com.mbd.cmscommon.ui.components.MoreDestination
import com.mbd.cmscommon.ui.components.MoreHubWorkspace
import com.mbd.cmscommon.ui.components.NotificationControllerWorkspace
import com.mbd.cmscommon.ui.components.PeopleDestination
import com.mbd.cmscommon.ui.components.PeopleHubWorkspace
import com.mbd.cmscommon.ui.components.RecordsDestination
import com.mbd.cmscommon.ui.components.RecordsHubWorkspace
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmsdesktop.di.DesktopAppComponent
import java.awt.Window
import kotlinx.coroutines.launch

private enum class AdminTab(val label: String, val icon: ImageVector) {
    Dashboard("Dashboard", Icons.Filled.Dashboard),
    Academics("Academics", Icons.Filled.School),
    People("People", Icons.Filled.Groups),
    Records("Records", Icons.Filled.Assessment),
    More("More", Icons.Filled.MoreHoriz),
}

private sealed interface AdminLeaf {
    data object Administrators : AdminLeaf
    data object LinkRequests : AdminLeaf
    data object MarkEditRequests : AdminLeaf
    data object MasterTimetable : AdminLeaf
    data object Notifications : AdminLeaf
    data object Profile : AdminLeaf
}

/**
 * Top-level shell for the admin desktop app: a [NavigationRail] over the 5 mobile-parity tabs
 * (see `AdminTab` in mobile-admin) + a handful of directly-reachable leaves. Deep drill-downs
 * (department -> session -> semester -> student profile chain, attendance records, session fees)
 * are not yet ported to desktop — Academics here is department list + CRUD only.
 */
@Composable
fun AdminNavHost(role: UserRole.Admin, component: DesktopAppComponent, window: Window, onSignOut: () -> Unit) {
    val scope = rememberCoroutineScope()
    var tab by remember { mutableStateOf(AdminTab.Dashboard) }
    var leaf by remember { mutableStateOf<AdminLeaf?>(null) }
    val accountKey = component.sessionManager().accountKey.orEmpty()

    Row(Modifier.fillMaxSize()) {
        NavigationRail(containerColor = CmsTheme.colors.ink, contentColor = CmsTheme.colors.onInk) {
            Spacer(Modifier.height(16.dp))
            AdminTab.entries.forEach { t ->
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
                        AdminLeaf.Administrators -> {
                            val controller = remember(component) {
                                AdministratorsController(component.administratorRepository(), accountKey, scope)
                            }
                            val administrators by controller.administrators.collectAsState()
                            val loading by controller.loading.collectAsState()
                            val creating by controller.creating.collectAsState()
                            val createdEmail by controller.createdEmail.collectAsState()
                            AdministratorDirectoryWorkspace(
                                administrators = administrators,
                                currentAccountKey = controller.currentAccountKey,
                                loading = loading,
                                creating = creating,
                                createdEmail = createdEmail,
                                errorMessage = null,
                                onRefresh = controller::refresh,
                                onCreate = controller::create,
                                onConsumeCreated = controller::consumeCreated,
                                onClearError = {},
                            )
                        }
                        AdminLeaf.LinkRequests -> {
                            val controller = remember(component) {
                                LinkRequestsController(
                                    repository = component.studentLinkRequestRepository(),
                                    sessionRepository = component.academicSessionRepository(),
                                    departmentRepository = component.departmentRepository(),
                                    reviewerId = accountKey,
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
                                errorMessage = null,
                                onRefresh = controller::refresh,
                                onApprove = controller::approve,
                                onReject = controller::reject,
                                onConsumeNotice = controller::consumeNotice,
                                onClearError = {},
                            )
                        }
                        AdminLeaf.MarkEditRequests -> {
                            val controller = remember(component) {
                                MarkEditRequestsController(
                                    component.markEditRequestRepository(),
                                    component.academicSessionRepository(),
                                    component.curriculumRepository(),
                                    component.departmentRepository(),
                                    component.teacherRepository(),
                                    accountKey,
                                    scope,
                                )
                            }
                            val requests by controller.requests.collectAsState()
                            val details by controller.details.collectAsState()
                            val sessions by controller.sessions.collectAsState()
                            val departments by controller.departments.collectAsState()
                            val teachers by controller.teachers.collectAsState()
                            val loading by controller.loading.collectAsState()
                            val busyRequestId by controller.busyRequestId.collectAsState()
                            val rowErrors by controller.rowErrors.collectAsState()
                            val notice by controller.notice.collectAsState()
                            MarkEditRequestReviewWorkspace(
                                requests = requests,
                                details = details,
                                sessions = sessions,
                                departments = departments,
                                teachers = teachers,
                                loading = loading,
                                busyRequestId = busyRequestId,
                                rowErrors = rowErrors,
                                notice = notice,
                                errorMessage = null,
                                onApprove = controller::approve,
                                onReject = controller::reject,
                                onRefresh = controller::refresh,
                                onConsumeNotice = controller::consumeNotice,
                                onClearError = {},
                            )
                        }
                        AdminLeaf.MasterTimetable -> {
                            val controller = remember(component) {
                                MasterTimetableController(
                                    component.departmentRepository(),
                                    component.academicSessionRepository(),
                                    component.sessionTimetableRepository(),
                                    scope,
                                )
                            }
                            val day by controller.day.collectAsState()
                            val shift by controller.shift.collectAsState()
                            val departments by controller.departments.collectAsState()
                            val sessions by controller.sessions.collectAsState()
                            val periods by controller.periods.collectAsState()
                            val loading by controller.loading.collectAsState()
                            val refreshError by controller.refreshError.collectAsState()
                            MasterTimetableWorkspace(
                                day = day,
                                shift = shift,
                                departments = departments,
                                sessions = sessions,
                                periods = periods,
                                loading = loading,
                                errorMessage = refreshError,
                                onDayChange = controller::selectDay,
                                onShiftChange = controller::selectShift,
                                onRetry = controller::refresh,
                                onOpenSession = {},
                            )
                        }
                        AdminLeaf.Notifications -> {
                            val controller = remember(component) {
                                com.mbd.cmscommon.controller.NotificationsController(
                                    repository = component.notificationRepository(),
                                    viewerRole = com.mbd.cmscommon.domain.model.NotificationTargetRole.ADMIN,
                                    accountKey = accountKey,
                                    sessionRepository = component.academicSessionRepository(),
                                    departmentRepository = component.departmentRepository(),
                                    publisherKind = com.mbd.cmscommon.controller.NotificationPublisherKind.ADMIN,
                                    scope = scope,
                                )
                            }
                            NotificationControllerWorkspace(controller = controller)
                        }
                        AdminLeaf.Profile -> {
                            val controller = remember(component) {
                                AdministratorsController(component.administratorRepository(), accountKey, scope)
                            }
                            val administrators by controller.administrators.collectAsState()
                            val account = administrators.find { it.email == accountKey }
                            AdministratorProfileWorkspace(
                                accountKey = accountKey,
                                account = account,
                                directory = null,
                                loading = false,
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
                    AdminTab.Dashboard -> {
                        val controller = remember(component) {
                            DashboardController(
                                component.academicSessionRepository(),
                                component.teacherRepository(),
                                component.departmentRepository(),
                                component.studentLinkRequestRepository(),
                                scope,
                            )
                        }
                        val state by controller.state.collectAsState()
                        AdminDashboardContent(
                            state = state,
                            heroPainter = androidx.compose.ui.res.painterResource("splash_postgraduate_block.jpg"),
                            actions = listOf(
                                DashboardActionUi("Master timetable", "Weekly schedule across every department", Icons.Filled.Assessment) { leaf = AdminLeaf.MasterTimetable },
                                DashboardActionUi("Link requests", "Approve pending student account links", Icons.Filled.Groups) { leaf = AdminLeaf.LinkRequests },
                            ),
                            onOpenMasterTimetable = { leaf = AdminLeaf.MasterTimetable },
                            onOpenLinkRequests = { leaf = AdminLeaf.LinkRequests },
                            onOpenNotifications = { leaf = AdminLeaf.Notifications },
                        )
                    }
                    AdminTab.Academics -> {
                        val controller = remember(component) {
                            DepartmentsActionController(component.departmentRepository(), accountKey, scope)
                        }
                        val departments by component.departmentRepository().observeActiveDepartments().collectAsState(initial = emptyList())
                        DepartmentPortfolio(
                            departments = departments,
                            stats = departmentPortfolioStats(emptyList()),
                            heroPainter = androidx.compose.ui.res.painterResource("splash_postgraduate_block.jpg"),
                            onOpenDepartment = {},
                            onEditDepartment = {},
                            onDeleteDepartment = { scope.launch { controller.delete(it.deptId) } },
                            onAddDepartment = {},
                        )
                    }
                    AdminTab.People -> {
                        val controller = remember(component) {
                            PeopleHubController(
                                component.administratorRepository(),
                                component.teacherRepository(),
                                component.academicSessionRepository(),
                                component.studentLinkRequestRepository(),
                                component.markEditRequestRepository(),
                                scope,
                            )
                        }
                        val snapshot by controller.snapshot.collectAsState()
                        val loading by controller.loading.collectAsState()
                        val loadError by controller.loadError.collectAsState()
                        PeopleHubWorkspace(
                            heroPainter = androidx.compose.ui.res.painterResource("splash_postgraduate_block.jpg"),
                            snapshot = snapshot,
                            loading = loading,
                            errorMessage = loadError,
                            onRetry = controller::refresh,
                            onOpen = { destination ->
                                when (destination) {
                                    PeopleDestination.ADMINISTRATORS -> leaf = AdminLeaf.Administrators
                                    PeopleDestination.TEACHERS -> {}
                                    PeopleDestination.STUDENTS -> tab = AdminTab.Academics
                                    PeopleDestination.LINK_REQUESTS -> leaf = AdminLeaf.LinkRequests
                                    PeopleDestination.MARK_EDIT_REQUESTS -> leaf = AdminLeaf.MarkEditRequests
                                }
                            },
                        )
                    }
                    AdminTab.Records -> {
                        val controller = remember(component) {
                            RecordsHubController(
                                component.academicSessionRepository(),
                                component.calendarRepository(),
                                component.datesheetRepository(),
                                component.documentRepository(),
                                component.insightsRepository(),
                                scope,
                            )
                        }
                        val snapshot by controller.snapshot.collectAsState()
                        val loading by controller.loading.collectAsState()
                        val loadError by controller.loadError.collectAsState()
                        RecordsHubWorkspace(
                            heroPainter = androidx.compose.ui.res.painterResource("splash_postgraduate_block.jpg"),
                            snapshot = snapshot,
                            loading = loading,
                            errorMessage = loadError,
                            onRetry = controller::refresh,
                            onOpen = { destination ->
                                when (destination) {
                                    RecordsDestination.TIMETABLE -> leaf = AdminLeaf.MasterTimetable
                                    else -> {}
                                }
                            },
                        )
                    }
                    AdminTab.More -> {
                        val controller = remember(component) {
                            MoreHubController(accountKey, component.administratorRepository(), component.notificationRepository(), scope)
                        }
                        val snapshot by controller.snapshot.collectAsState()
                        val loading by controller.loading.collectAsState()
                        val loadError by controller.loadError.collectAsState()
                        MoreHubWorkspace(
                            heroPainter = androidx.compose.ui.res.painterResource("splash_postgraduate_block.jpg"),
                            snapshot = snapshot,
                            loading = loading,
                            errorMessage = loadError,
                            onRetry = controller::refresh,
                            onOpen = { destination ->
                                when (destination) {
                                    MoreDestination.NOTIFICATIONS -> leaf = AdminLeaf.Notifications
                                    MoreDestination.PROFILE -> leaf = AdminLeaf.Profile
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

private fun leafTitle(leaf: AdminLeaf): String = when (leaf) {
    AdminLeaf.Administrators -> "Administrators"
    AdminLeaf.LinkRequests -> "Link requests"
    AdminLeaf.MarkEditRequests -> "Mark edit requests"
    AdminLeaf.MasterTimetable -> "Master timetable"
    AdminLeaf.Notifications -> "Notifications"
    AdminLeaf.Profile -> "Profile"
}
