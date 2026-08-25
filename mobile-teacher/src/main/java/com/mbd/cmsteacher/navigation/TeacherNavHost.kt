package com.mbd.cmsteacher.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mbd.cmscommon.ui.datesheets.DatesheetsScreen
import com.mbd.cmscommon.ui.documents.DocumentsScreen
import com.mbd.cmscommon.ui.events.EventsScreen
import com.mbd.cmsteacher.feature.attendance.AttendanceHistoryScreen
import com.mbd.cmsteacher.feature.attendance.MarkAttendanceScreen
import com.mbd.cmsteacher.feature.exams.ExamPaperSubmissionScreen
import com.mbd.cmsteacher.feature.exams.ExamsHubScreen
import com.mbd.cmsteacher.feature.home.HomeScreen
import com.mbd.cmsteacher.feature.hub.MenuHubScreen
import com.mbd.cmsteacher.feature.insights.InsightsScreen
import com.mbd.cmsteacher.feature.linkrequests.LinkRequestsScreen
import com.mbd.cmsteacher.feature.marks.MarksEntryScreen
import com.mbd.cmsteacher.feature.notifications.NotificationsScreen
import com.mbd.cmsteacher.feature.profile.ProfileScreen
import com.mbd.cmsteacher.feature.results.SemesterResultsScreen
import com.mbd.cmsteacher.feature.schedule.ScheduleScreen
import com.mbd.cmsteacher.feature.students.MyStudentsScreen

@Composable
fun TeacherNavHost(navController: NavHostController, onSignedOut: () -> Unit, refreshVersion: Int = 0) {
    fun go(route: String) = navController.navigate(route)

    NavHost(navController = navController, startDestination = TeacherDestination.Home.route) {
        composable(TeacherDestination.Home.route) { HomeScreen(onOpen = ::go) }
        composable(TeacherDestination.Attendance.route) { MarkAttendanceScreen(onOpenHistory = ::go) }
        composable(TeacherDestination.ExamsHub.route) { ExamsHubScreen(onOpen = ::go) }
        composable(TeacherDestination.Schedule.route) { ScheduleScreen() }
        composable(TeacherDestination.MenuHub.route) {
            MenuHubScreen(
                onOpenMyStudents = { go(TeacherDestination.MyStudents.route) },
                onOpenCalendar = { go(TeacherDestination.Events.route) },
                onOpenDocuments = { go(TeacherDestination.Documents.route) },
                onOpenInsights = { go(TeacherDestination.Insights.route) },
                onOpenLinkRequests = { go(TeacherDestination.LinkRequests.route) },
                onOpenNotifications = { go(TeacherDestination.Notifications.route) },
                onOpenProfile = { go(TeacherDestination.Profile.route) },
                onSignedOut = onSignedOut,
            )
        }

        composable(TeacherDestination.Marks.route) { MarksEntryScreen() }
        composable(TeacherDestination.SemesterResults.route) { SemesterResultsScreen() }
        composable(TeacherDestination.ExamPaper.route) { ExamPaperSubmissionScreen() }
        composable(TeacherDestination.Notifications.route) { NotificationsScreen(refreshVersion = refreshVersion) }
        composable(TeacherDestination.LinkRequests.route) { LinkRequestsScreen() }
        composable(TeacherDestination.MyStudents.route) { MyStudentsScreen() }
        composable(TeacherDestination.Events.route) { EventsScreen(viewModel = hiltViewModel()) }
        composable(TeacherDestination.Datesheets.route) { DatesheetsScreen(viewModel = hiltViewModel()) }
        composable(TeacherDestination.Documents.route) { DocumentsScreen(viewModel = hiltViewModel()) }
        composable(TeacherDestination.Insights.route) { InsightsScreen(refreshVersion = refreshVersion) }
        composable(TeacherDestination.Profile.route) { ProfileScreen(onSignedOut = onSignedOut) }
        composable(TeacherDestination.AttendanceHistory.route) { AttendanceHistoryScreen() }
    }
}
