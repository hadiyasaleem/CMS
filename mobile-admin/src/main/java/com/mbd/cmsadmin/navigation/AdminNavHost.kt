package com.mbd.cmsadmin.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mbd.cmsadmin.feature.academics.MasterTimetableScreen
import com.mbd.cmsadmin.feature.academics.SemesterSubjectsScreen
import com.mbd.cmsadmin.feature.academics.SessionDetailScreen
import com.mbd.cmsadmin.feature.academics.SessionStudentsScreen
import com.mbd.cmsadmin.feature.academics.SessionTimetableScreen
import com.mbd.cmsadmin.feature.academics.StudentProfileScreen
import com.mbd.cmsadmin.feature.administrators.AdministratorsScreen
import com.mbd.cmsadmin.feature.dashboard.DashboardScreen
import com.mbd.cmsadmin.feature.departments.DepartmentDetailScreen
import com.mbd.cmsadmin.feature.departments.DepartmentsScreen
import com.mbd.cmsadmin.feature.hub.MoreHubScreen
import com.mbd.cmsadmin.feature.hub.PeopleHubScreen
import com.mbd.cmsadmin.feature.hub.RecordsHubScreen
import com.mbd.cmsadmin.feature.linkrequests.LinkRequestsScreen
import com.mbd.cmsadmin.feature.notifications.NotificationsScreen
import com.mbd.cmsadmin.feature.profile.ProfileScreen
import com.mbd.cmsadmin.feature.teachers.TeachersScreen
import com.mbd.cmscommon.ui.components.RecordsDestination
import com.mbd.cmscommon.ui.components.PeopleDestination
import com.mbd.cmscommon.ui.components.MoreDestination

@Composable
fun AdminNavHost(navController: NavHostController, onSignedOut: () -> Unit, refreshVersion: Int = 0) {
    fun go(route: String) = navController.navigate(route)

    NavHost(navController = navController, startDestination = AdminTab.Dashboard.route) {
        // ── Tabs ──
        composable(AdminTab.Dashboard.route) { DashboardScreen(onOpen = ::go) }

        // Academics = departments, directly. Each department owns its curriculum,
        // sessions, and fee structure — no terms/offerings detours.
        composable(AdminTab.Academics.route) {
            DepartmentsScreen(onOpenDepartment = { go(AdminRoutes.deptDetail(it)) })
        }

        composable(AdminTab.People.route) {
            PeopleHubScreen(onOpen = { destination ->
                go(
                    when (destination) {
                        PeopleDestination.ADMINISTRATORS -> AdminLeaf.ADMINISTRATORS
                        PeopleDestination.TEACHERS -> AdminLeaf.TEACHERS
                        PeopleDestination.STUDENTS -> AdminTab.Academics.route
                        PeopleDestination.LINK_REQUESTS -> AdminLeaf.LINK_REQUESTS
                        PeopleDestination.MARK_EDIT_REQUESTS -> AdminLeaf.MARK_EDIT_REQUESTS
                    },
                )
            })
        }

        composable(AdminTab.Records.route) {
            RecordsHubScreen(onOpen = { destination ->
                go(
                    when (destination) {
                        RecordsDestination.ATTENDANCE -> AdminLeaf.ATTENDANCE_RECORDS
                        RecordsDestination.CALENDAR -> AdminLeaf.CALENDAR
                        RecordsDestination.DATESHEETS -> AdminLeaf.DATESHEETS
                        RecordsDestination.TIMETABLE -> AdminLeaf.MASTER_TIMETABLE
                        RecordsDestination.FEES -> AdminLeaf.FEES_PICKER
                        RecordsDestination.INSIGHTS -> AdminLeaf.INSIGHTS
                    },
                )
            })
        }

        composable(AdminTab.More.route) {
            MoreHubScreen(onOpen = { destination ->
                go(
                    when (destination) {
                        MoreDestination.NOTIFICATIONS -> AdminLeaf.NOTIFICATIONS
                        MoreDestination.PROFILE -> AdminLeaf.PROFILE
                    },
                )
            })
        }

        // ── Leaves ──
        composable(AdminLeaf.ADMINISTRATORS) { AdministratorsScreen() }
        composable(AdminLeaf.TEACHERS) { TeachersScreen() }
        composable(AdminLeaf.LINK_REQUESTS) { LinkRequestsScreen() }
        composable(AdminLeaf.MARK_EDIT_REQUESTS) {
            com.mbd.cmsadmin.feature.markrequests.MarkEditRequestsScreen(refreshVersion = refreshVersion)
        }
        composable(AdminLeaf.NOTIFICATIONS) { NotificationsScreen(refreshVersion = refreshVersion) }
        composable(AdminLeaf.PROFILE) { ProfileScreen(onSignedOut = onSignedOut) }
        composable(AdminLeaf.MASTER_TIMETABLE) {
            MasterTimetableScreen(onOpenSession = { go(AdminRoutes.sessionTimetable(it)) })
        }
        // "Fee Structures" drills department → session → that session's fee structure (fees are per-session).
        composable(AdminLeaf.FEES_PICKER) {
            DepartmentsScreen(onOpenDepartment = { go(AdminRoutes.deptDetail(it)) })
        }
        composable(AdminLeaf.ATTENDANCE_RECORDS) {
            com.mbd.cmsadmin.feature.records.AttendanceRecordsScreen()
        }
        composable(AdminLeaf.CALENDAR) { com.mbd.cmsadmin.feature.calendar.CalendarScreen() }
        composable(AdminLeaf.DATESHEETS) { com.mbd.cmsadmin.feature.datesheets.DatesheetsScreen() }
        composable(AdminLeaf.INSIGHTS) { com.mbd.cmsadmin.feature.insights.InsightsScreen(refreshVersion = refreshVersion) }

        // ── Department drill-down ──
        composable(AdminRoutes.DEPT_DETAIL) { backStackEntry ->
            val deptId = checkNotNull(backStackEntry.arguments?.getString("deptId"))
            DepartmentDetailScreen(
                deptId = deptId,
                onOpenSession = { go(AdminRoutes.sessionDetail(it)) },
            )
        }

        // ── Session drill-down ──
        composable(AdminRoutes.SESSION_DETAIL) {
            SessionDetailScreen(
                onOpenStudents = { go(AdminRoutes.sessionStudents(it)) },
                onOpenTimetable = { go(AdminRoutes.sessionTimetable(it)) },
                onOpenSemester = { sid, semester -> go(AdminRoutes.semesterSubjects(sid, semester)) },
                onOpenFees = { go(AdminRoutes.sessionFees(it)) },
                onDeleted = { navController.popBackStack() },
            )
        }
        composable(AdminRoutes.SESSION_FEES) { com.mbd.cmsadmin.feature.academics.SessionFeesScreen() }
        composable(AdminRoutes.SEMESTER_SUBJECTS) { SemesterSubjectsScreen() }
        composable(AdminRoutes.SESSION_STUDENTS) {
            SessionStudentsScreen(onOpenStudent = { sid, roll -> go(AdminRoutes.studentProfile(sid, roll)) })
        }
        composable(AdminRoutes.STUDENT_PROFILE) { StudentProfileScreen() }
        composable(AdminRoutes.SESSION_TIMETABLE) { SessionTimetableScreen() }
    }
}
