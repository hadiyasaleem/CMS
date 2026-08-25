package com.mbd.cmsstudent.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.mbd.cmscommon.ui.components.StudentHomeDestination
import com.mbd.cmscommon.ui.components.StudentHomeWorkspace
import com.mbd.cmsstudent.R
import com.mbd.cmsstudent.navigation.StudentDestination

@Composable
fun HomeScreen(onOpen: (String) -> Unit, viewModel: HomeViewModel = hiltViewModel()) {
    val snapshot by viewModel.snapshot.collectAsState()

    StudentHomeWorkspace(
        heroPainter = painterResource(R.drawable.student_home_hero),
        snapshot = snapshot,
        loading = snapshot == null,
        onOpen = { destination ->
            onOpen(
                when (destination) {
                    StudentHomeDestination.ATTENDANCE -> StudentDestination.Attendance.route
                    StudentHomeDestination.MARKS -> StudentDestination.Marks.route
                    StudentHomeDestination.TIMETABLE -> StudentDestination.Timetable.route
                    StudentHomeDestination.FEES -> StudentDestination.Fees.route
                },
            )
        },
    )
}
