package com.mbd.cmsstudent.feature.attendance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.mbd.cmscommon.ui.components.StudentAttendanceWorkspace
import com.mbd.cmsstudent.R

@Composable
fun AttendanceSummaryScreen(viewModel: AttendanceSummaryViewModel = hiltViewModel()) {
    val snapshot by viewModel.snapshot.collectAsState()

    StudentAttendanceWorkspace(
        heroPainter = painterResource(R.drawable.student_attendance_hero),
        snapshot = snapshot,
        loading = snapshot == null,
    )
}
