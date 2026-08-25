package com.mbd.cmsstudent.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mbd.cmscommon.ui.datesheets.DatesheetsScreen
import com.mbd.cmscommon.ui.documents.DocumentsScreen
import com.mbd.cmscommon.ui.events.EventsScreen
import com.mbd.cmsstudent.feature.attendance.AttendanceSummaryScreen
import com.mbd.cmsstudent.feature.exams.ExamsHubScreen
import com.mbd.cmsstudent.feature.fees.FeeChallanScreen
import com.mbd.cmsstudent.feature.home.HomeScreen
import com.mbd.cmsstudent.feature.hub.MoreHubScreen
import com.mbd.cmsstudent.feature.marks.MyMarksScreen
import com.mbd.cmsstudent.feature.notifications.NotificationsScreen
import com.mbd.cmsstudent.feature.profile.ProfileScreen
import com.mbd.cmsstudent.feature.results.ResultsScreen
import com.mbd.cmsstudent.feature.timetable.MyTimetableScreen

@Composable
fun StudentNavHost(navController: NavHostController, onSignedOut: () -> Unit, refreshVersion: Int = 0) {
    fun go(route: String) = navController.navigate(route)

    NavHost(navController = navController, startDestination = StudentDestination.Home.route) {
        composable(StudentDestination.Home.route) { HomeScreen(onOpen = ::go) }
        composable(StudentDestination.Attendance.route) { AttendanceSummaryScreen() }
        composable(StudentDestination.ExamsHub.route) { ExamsHubScreen(onOpen = ::go) }
        composable(StudentDestination.Timetable.route) { MyTimetableScreen() }
        composable(StudentDestination.More.route) { MoreHubScreen(onOpen = ::go, onSignOut = onSignedOut) }

        composable(StudentDestination.Marks.route) { MyMarksScreen() }
        composable(StudentDestination.Results.route) { ResultsScreen() }
        composable(StudentDestination.Events.route) { EventsScreen(viewModel = hiltViewModel()) }
        composable(StudentDestination.Datesheets.route) { DatesheetsScreen(viewModel = hiltViewModel()) }
        composable(StudentDestination.Documents.route) { DocumentsScreen(viewModel = hiltViewModel()) }
        composable(StudentDestination.Fees.route) { FeeChallanScreen() }
        composable(StudentDestination.Notifications.route) { NotificationsScreen(refreshVersion = refreshVersion) }
        composable(StudentDestination.Profile.route) { ProfileScreen(onSignedOut = onSignedOut) }
    }
}
