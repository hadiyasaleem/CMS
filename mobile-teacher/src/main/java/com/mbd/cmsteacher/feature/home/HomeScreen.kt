package com.mbd.cmsteacher.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.mbd.cmscommon.ui.components.TeacherHomeDestination
import com.mbd.cmscommon.ui.components.TeacherHomeWorkspace
import com.mbd.cmsteacher.R
import com.mbd.cmsteacher.navigation.TeacherDestination

@Composable
fun HomeScreen(onOpen: (String) -> Unit, viewModel: HomeViewModel = hiltViewModel()) {
    val snapshot by viewModel.snapshot.collectAsState()

    TeacherHomeWorkspace(
        heroPainter = painterResource(R.drawable.teacher_home_hero),
        snapshot = snapshot,
        onOpen = { destination ->
            onOpen(
                when (destination) {
                    TeacherHomeDestination.ATTENDANCE -> TeacherDestination.Attendance.route
                    TeacherHomeDestination.MARKS -> TeacherDestination.Marks.route
                    TeacherHomeDestination.EXAM_PAPER -> TeacherDestination.ExamPaper.route
                    TeacherHomeDestination.STUDENTS -> TeacherDestination.MyStudents.route
                    TeacherHomeDestination.SCHEDULE -> TeacherDestination.Schedule.route
                    TeacherHomeDestination.NOTIFICATIONS -> TeacherDestination.Notifications.route
                },
            )
        },
    )
}
