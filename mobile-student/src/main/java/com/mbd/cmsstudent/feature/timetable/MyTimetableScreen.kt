package com.mbd.cmsstudent.feature.timetable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.mbd.cmscommon.ui.components.StudentTimetableWorkspace
import com.mbd.cmsstudent.R

@Composable
fun MyTimetableScreen(viewModel: MyTimetableViewModel = hiltViewModel()) {
    val snapshot by viewModel.snapshot.collectAsState()

    StudentTimetableWorkspace(
        heroPainter = painterResource(R.drawable.student_timetable_hero),
        snapshot = snapshot,
        loading = snapshot == null,
        errorMessage = null,
        onRetry = viewModel::refresh,
    )
}
