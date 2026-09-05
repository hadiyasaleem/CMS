package com.mbd.cmsteacher.feature.schedule

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.mbd.cmscommon.ui.components.TeacherScheduleWorkspace
import com.mbd.cmsteacher.R

@Composable
fun ScheduleScreen(viewModel: ScheduleViewModel = hiltViewModel()) {
    val periods by viewModel.periods.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val outcome by viewModel.outcome.collectAsState()

    TeacherScheduleWorkspace(
        heroPainter = painterResource(R.drawable.teacher_schedule_hero),
        periods = periods,
        sessions = sessions,
        outcome = outcome,
        onRefresh = viewModel::refresh,
    )
}
