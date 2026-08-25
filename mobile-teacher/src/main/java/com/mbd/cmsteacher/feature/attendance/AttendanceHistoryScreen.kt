package com.mbd.cmsteacher.feature.attendance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.mbd.cmscommon.ui.components.AttendanceHistoryWorkspace

@Composable
fun AttendanceHistoryScreen(viewModel: AttendanceHistoryViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val monthLabel by viewModel.monthLabel.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val roster by viewModel.roster.collectAsState()
    val marks by viewModel.marks.collectAsState()

    AttendanceHistoryWorkspace(
        courseCode = viewModel.courseCode,
        monthLabel = monthLabel,
        loading = loading,
        roster = roster,
        marks = marks,
        onPreviousMonth = viewModel::previousMonth,
        onNextMonth = viewModel::nextMonth,
        onExportCsv = { viewModel.exportCsv(context) },
        onExportPdf = { viewModel.exportPdf(context) },
    )
}
